# coding: utf-8
import os
import sys
import robot
import argparse

from robot import pythonpathsetter


parser = argparse.ArgumentParser(description='run robot test case.')
parser.add_argument('--skip-build', dest='skip_build', const=True, nargs="?",
                    help=u'本参数在没有输入 filename 时生效，用于跳过打包测试')
parser.add_argument('filename', default='', nargs="?",
                    help='robot test case filename, e.g. robot/package.robot')
parser.add_argument('--test_case', '-t', default='', nargs="?",
                    help=u'robot test case  e.g. 构建nginx安装包')
args = parser.parse_args()

project_dir = os.path.dirname(os.path.abspath(__file__))
pythonpathsetter.add_path(project_dir)

def exit(code):
    pythonpathsetter.remove_path(project_dir)
    sys.exit(code)


def convert_code(str):
    try:
        return str.decode('utf8')
    except:
        return str.decode('gbk')


if not args.filename:
    if not args.skip_build and robot.run('robot/package.robot') != 0:
        print ('run package test failed')
        exit(-1)

    if robot.run('robot/install_omp_module.robot') != 0:
        print ('run install omp module test failed')
        exit(-2)

    if robot.run('robot/install_agent.robot') != 0:
        print ('run install agent test failed')
        exit(-3)

    if robot.run('robot/install_module.robot') != 0:
        print ('run install module test failed')
        exit(-4)

    if robot.run('robot/upgrade_module.robot') != 0:
        print ('run upgrade module test failed')
        exit(-5)
else:
    options = {}
    if args.test_case:
        options['test'] = convert_code(args.test_case)
    robot.run(args.filename, **options)

pythonpathsetter.remove_path(project_dir)