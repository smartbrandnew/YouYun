def is_virtual_machine():
    from win32com.client import GetObject
    wmi = GetObject('winmgmts:/root/cimv2')
    computer_system = wmi.execquery('select * from Win32_ComputerSystem')[0]
    for keyword in ('kvm', 'hvm', 'virutal', 'vmware'):
        if keyword in computer_system.model:
            return True
    return False
