# Pull base image
FROM centos
MAINTAINER uyun

# USER root
RUN yum clean all; \
    rpm --rebuilddb; \
    yum install -y curl which tar sudo openssh-server openssh-clients rsync; \
    yum install -y vim mlocate sudo ntpdate net-tools telnet git wget iproute initscripts; \
    yum update -y libselinux; \
    updatedb; \
    ssh-keygen -q -N "" -t dsa -f /etc/ssh/ssh_host_dsa_key; \
    ssh-keygen -q -N "" -t rsa -f /etc/ssh/ssh_host_rsa_key; \
    ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa; \
    cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys; \
    mkdir /var/run/sshd; \
    echo "nameserver 10.1.2.252" >> /etc/resolv.conf; \
    echo '123456' | passwd --stdin root
    # Expose ports.

EXPOSE 80 22
ENV LANG zh_CN.UTF-8