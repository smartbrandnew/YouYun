#!/C:/Python27

# from ping_task import PingTask
# from ping_request import PingRequest


class WaitItem():
    def __init__(self, task, waitTime):
        self.task = task
        self.waitTime = waitTime

    def compare_to(self, obj):
        if self.waitTime < obj.waitTime:
            return -1
        elif self.waitTime == obj.waitTime:
            if self.task.getId() < obj.task.getId():
                return -1
            elif self.task.getId() == obj.task.getId():
                return 0
        return 1

    def get_task(self):
        return self.task

    def get_wait_time(self):
        return self.waitTime

    def is_timeout(self, now):
        return now >= self.waitTime

    def hashcode(self):
        return self.hashcode()

    def equals(self, obj):
        return self.task == obj.task  # if the content are same

if __name__ == '__main__':
    request1 = PingRequest('www.baidu.com')
    task1 = PingTask(request1)
    test1 = WaitItem(task1, task1.getCreateTime())

    request2 = PingRequest('www.bing.com')
    task2 = PingTask(request2)
    test2 = WaitItem(task2, task2.getCreateTime())

    print test1.equals(test2)
    print test1.compare_to(test2)
