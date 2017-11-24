from test_payload import payload,test_url,agent_build_url
import requests
import json

def test_agent_unit_test(url):
    payload['repository']['name'] = 'agent'
    payload['repository']['url'] = 'git@git.uyunsoft.cn:antman/agent.git'
    requests.post(url,json=payload)

def test_dispatcher_deploy(url):
    payload['repository']['name'] = 'dispatcher'
    payload['repository']['url'] = 'git@git.uyunsoft.cn:antman/dispatcher.git'
    requests.post(url,json=payload)

def test_manager_deploy(url):
    payload['repository']['name'] = 'manager'
    payload['repository']['url'] = 'git@git.uyunsoft.cn:antman/manager.git'
    requests.post(url,json=payload)

def test_manager_web_deploy(url):
    payload['repository']['name'] = 'manager-web'
    payload['repository']['url'] = 'git@git.uyunsoft.cn:antman/manager-web.git'
    requests.post(url,json=payload)

def test_agent_deploy(url):
    payload['repository']['name'] = 'agent'
    payload['repository']['url'] = 'git@git.uyunsoft.cn:antman/agent.git'
    requests.post(url,json=payload)
    

#map(test_agent_unit_test,test_url)
#test_dispatcher_deploy('http://10.1.100.221:8000/ci')
#test_manager_deploy('http://10.1.100.221:8000/ci')
#test_manager_web_deploy('http://10.1.221.220:8000/ci')
test_agent_deploy('http://10.1.100.221:8000/ci')