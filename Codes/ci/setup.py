import os
import sys
try:
    from setuptools import setup, find_packages
except ImportError:
    from distribute_setup import use_setuptools, find_packages
    use_setuptools()
    from setuptools import setup


HERE = os.path.abspath(os.path.dirname(__file__))


def get_version():
    sys.path.append(HERE)
    import ci
    return ci.__version__
print(find_packages())
setup(
    name='ci',
    version=get_version(),
    description='ci',
    author='Ant Team',
    url='https://git.uyunsoft.cn/antman/ci',
    packages=find_packages(),
    include_package_data=True,
    install_requires=['tornado', 'pf', 'nfs', 'ddc']
)
