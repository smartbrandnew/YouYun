# -*- coding: UTF-8 -*-
# !/C:/Python27

import time
import socket
import struct
import select
import logging
# from ping_request import PingRequest
from pinger import PingTask, WaitItem, PingPacket, PingListener
from threading import Thread, Event, Lock

__author__ = 'fangjc'
__project__ = 'uyun-bat-ping'
__date__ = '2016/04/01'

ICMP_ECHO_REQUEST = 8
DEFAULT_SEND_PER_SEC = 100
DEFAULT_PING_WAIT_SENDS = 3000

log = logging.getLogger(__name__)
log.setLevel(logging.DEBUG)


class PingService():
    def __init__(self):
        self.wait_sends = []
        self.wait_responses = []
        self.running = None
        self.last_ttl = None
        self.sock = self.make_sock()
        self.wait_sends_event = Event()
        self.wait_responses_event = Event()
        self.wait_sends_lock = Lock()
        self.wait_responses_lock = Lock()

    # 开始监听任务
    def startup(self):
        self.running = True

        sender_thread = Thread(target=self.sender, args=())
        receiver_thread = Thread(target=self.receiver, args=())

        sender_thread.start()
        receiver_thread.start()

    # 停止任务监听退出程序
    def shutdown(self):
        self.running = False
        self.wait_sends_event.set()
        self.wait_responses_event.set()

    def sender(self):
        global DEFAULT_SEND_PER_SEC
        try:
            loop_interval = 1 / DEFAULT_SEND_PER_SEC
            while self.running:
                # 1 检查wait_sends等待发送队列
                while self.running:
                    if self.wait_sends == []:
                            self.wait_sends_event.wait()
                    else:
                        self.wait_sends_lock.acquire()
                        nex = self.wait_sends[0]
                        then = nex.get_wait_time()
                        now = time.time()
                        if then <= now:
                            self.wait_sends = self.wait_sends[1:]
                            self.wait_sends_lock.release()
                            break
                        self.wait_sends_lock.release()
                        time.sleep(then - now)
                # 2 发送
                task = nex.get_task()
                request = task.get_request()
                packet = PingPacket()
                target_addr = request.get_ip()
                size = request.get_size()
                ttl = request.get_ttl()
                seq = task.get_id() & 0xFFFF

                if ttl != self.last_ttl:
                    self.sock.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)
                    self.last_ttl = ttl
                try:
                    if not self.send_ping(self.sock, seq, target_addr, size):
                        log.info(
                            "Send ping packet failed: %s %s" % (task, packet)
                        )
                except socket.gaierror, e:
                    log.warn("Due to socket error %s" % e)
                    log.info("Send ping packet failed: %s %s" % (task, packet))
                packet.set_sent_time(time.time())
                task.add_packet(packet)

                # 3 如果发送成功，将任务加到wait_responses等待接收队列
                self.wait_responses_lock.acquire()
                self.wait_responses.append(
                    WaitItem(
                        task, packet.get_sent_time() +
                        request.get_timeout() / 1000
                    )
                )
                self.wait_responses_event.set()
                self.wait_responses_event.clear()
                self.wait_responses_lock.release()
                # 4 休眠一段时间
                time.sleep(loop_interval)
        except SystemExit, e:
            log.debug("Sender exit by command", e)
        except Exception, e:
            log.exception("An error occurred in Sender!", e)

    def receiver(self):
        try:
            while self.running:
                # 1 确定读取数据需要等待的时间
                while self.running:
                    if self.wait_responses == []:
                        self.wait_responses_event.wait()
                    else:
                        self.wait_responses_lock.acquire()
                        now = time.time()
                        period = self.wait_responses[0].get_wait_time() - now
                        self.wait_responses_lock.release()
                        if period <= 0:
                            period = 1 / 1000.0
                        break
                # 2 接收数据
                recv_packet = []
                addr = []
                readable = select.select([self.sock], [], [], period)
                received = readable[0]
                if received:
                    recv_packet, addr = self.sock.recvfrom(1024)
                    icmp_header1 = recv_packet[20:28]
                    icmp_header2 = recv_packet[:8]
                    kind1, code1, checksum1, packet_ID1, sequence1 = \
                        struct.unpack("bbHHH", icmp_header1)
                    kind2, code2, checksum2, packet_ID2, sequence2 = \
                        struct.unpack("bbHHH", icmp_header2)
                # 3 寻找接收到的任务与超时任务
                remove = None
                timeout_items = []
                now = time.time()
                self.wait_responses_lock.acquire()
                for item in self.wait_responses:
                    if item.is_timeout(now):
                        timeout_items.append(item)
                    elif received:
                        seq = item.get_task().get_id() & 0xFFFF
                        if seq == sequence1 or seq == sequence2:
                            remove = item
                for item in timeout_items:
                    self.wait_responses.remove(item)
                if remove:
                    self.wait_responses.remove(remove)
                self.wait_responses_lock.release()

                # 4 处理接收到的任务
                if remove is not None:
                    task = remove.get_task()
                    task.get_current_packet().set_received_time(now)
                    self.process_task(task)
                # 5 处理超时任务
                for item in timeout_items:
                    task = item.get_task()
                    task.get_current_packet().set_failed()
                    self.process_task(task)
        except SystemExit, e:
            log.debug("Receiver exit by command %s" % e)
        except Exception, e:
            log.exception("An error occurred in Receiver! %s" % e)

    # 制作socket
    def make_sock(self):
        icmp = socket.getprotobyname("icmp")
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, icmp)
        except socket.error:
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
            except socket.error, (errno, msg):
                if errno == 1:
                    msg += " ICMP messages can only be sent from root user \
                    processes"
                    raise socket.error(msg)
        except Exception, e:
            log.exception("Exception: %s" % e)
            return None
        return sock

    # 计算ICMP报文校验和
    def do_checksum(self, source_string):
        length = len(source_string)
        total = 0
        for i in xrange(0, length, 2):
            total += ord(source_string[i + 1]) + (ord(source_string[i]) << 8)
        while(total >> 16):
            total = (total & 0xFFFF) + (total >> 16)

        total = ~total & 0xFFFF
        return total

    # 发送ICMP报文
    def send_ping(self, sock, seq, target_addr, size):
        global ICMP_ECHO_REQUEST

        my_checksum = 0

        header = struct.pack("bbHHH", ICMP_ECHO_REQUEST, 0, 0, 0, seq)
        bytes_In_double = struct.calcsize("d")
        data = (size - bytes_In_double) * "Q"
        data = struct.pack("d", time.time()) + data

        my_checksum = self.do_checksum(header + data)
        header = struct.pack(
            "bbHHH", ICMP_ECHO_REQUEST, 0, socket.htons(my_checksum), 0, seq)
        packet = header + data
        return sock.sendto(packet, (target_addr, 0))

    # 处理超时或成功接收的人物
    def process_task(self, task):
        if task.is_finished():
            task.get_listener().finished(
                task.get_request(), task.create_response()
            )
        else:
            self.add_wait_send(
                task,
                task.get_current_packet().get_sent_time() +
                task.get_request().get_timeout() / 1000
            )

    # 将等待发送任务加入等待发送队列
    def add_wait_send(self, task, time):
        self.wait_sends_lock.acquire()
        self.wait_sends.append(WaitItem(task, time))
        self.wait_sends_event.set()
        self.wait_sends_event.clear()
        self.wait_sends_lock.release()

    # 返回回应结果并赋值，程序运行缓慢
    def ping(self, request, listener=PingListener()):
        global DEFAULT_PING_WAIT_SENDS
        task = PingTask(request, listener)
        self.add_wait_send(task, task.get_create_time())
        if listener.__class__.__name__ == "PingListener":
            timeout = (
                request.get_count() * request.get_timeout() +
                DEFAULT_PING_WAIT_SENDS
                ) / 1000.0
            return listener.wait_response(timeout)

    # 取得当前实例两个队列中任务的数目
    def get_request_count(self):
        return len(self.wait_sends) + len(self.wait_responses)

if __name__ == '__main__':
    ps = PingService()
    ps.startup()

    response1 = ps.ping(PingRequest("127.0.0.1", 1000, 2, 127, 7))
    response2 = ps.ping(PingRequest("9.9.9.9", 1000, 2, 127, 70))
    response3 = ps.ping(PingRequest("10.1.2.252", 1000, 2, 1, 70))
    response4 = ps.ping(PingRequest("10.1.2.252", 1000, 2, 2, 70))
    response5 = ps.ping(PingRequest('www.sina.com'))
    response6 = ps.ping(PingRequest('www.zhihu.com'))
    response7 = ps.ping(PingRequest('www.sohu.com'))
    response8 = ps.ping(PingRequest('www.github.com'))

    print response1
    print response2
    print response3
    print response4
    print response5
    print response6
    print response7
    print response8

    ps.shutdown()
