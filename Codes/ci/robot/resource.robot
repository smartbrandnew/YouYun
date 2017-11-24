*** Settings ***
Documentation    Suite description
Library    ../library/PackageLibrary.py
Library    ../library/InstallLibrary.py
Library    ../library/CommonLibrary.py
Library    ../library/ManagerLibrary.py


*** Variables ***
${126_host}               10.1.100.126
${126_username}           root
${126_password}           ant123456

${api_key}