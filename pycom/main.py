from network import Bluetooth
import pycom
import time

# https://docs.pycom.io/pycom_esp32/library/network.Bluetooth.html
# https://support.kontakt.io/hc/en-gb/articles/201492492-iBeacon-advertising-packet-structure
# https://github.com/google/eddystone/blob/master/protocol-specification.md
# https://github.com/google/eddystone/tree/master/eddystone-url/
# https://forum.pycom.io/topic/1120/creating-an-eddystone-beacon
# https://www.mkompf.com/tech/eddystoneurl.html

MODE_IBEACON = 1
MODE_EDDYSTONEURL = 2
mode = MODE_EDDYSTONEURL

def twocompl(x):
    if x < 0:
        x += 256

    return x        

timeSleep = 1

bluetooth = Bluetooth()

tx_power=0xEE

if mode == MODE_IBEACON:
    major = 22240
    minor = 234

    data = [
        0x4c, 0x00, 0x02, 0x15, # Manufacturer data
        0xA6, 0xE3, 0xE0, 0x06, 0xBB, 0xC4, 0x4D, 0xD1, 0x9C, 0xA3, 0x3E, 0x86, 0xC4, 0x95, 0x7D, 0xF0, # UUID (CliqTags)
        (major >> 8) & 0xFF, major & 0xFF, # Major ID
        (minor >> 8) & 0xFF, minor & 0xFF, # Minor ID
        tx_power #twocompl(power1m) # Power
    ]

    bluetooth.set_advertisement(name = None, manufacturer_data = bytes(data))
elif mode == MODE_EDDYSTONEURL:
    
    url = '/video/1/' #change final number for each pycom
    

    uuid = [0xfb, 0x34, 0x9b, 0x5f, 0x80, 0x00, 0x00, 0x80, 0x00, 0x10, 0x00, 0x00, 0xaa, 0xfe, 0x00, 0x00]

    url = url.replace('http://www.', chr(0))
    url = url.replace('https://www.', chr(1))
    url = url.replace('http://', chr(2))
    url = url.replace('https://', chr(3))

    url = url.replace('.com/', chr(0))
    url = url.replace('.org/', chr(1))
    url = url.replace('.edu/', chr(2))
    url = url.replace('.net/', chr(3))
    url = url.replace('.info/', chr(4))
    url = url.replace('.biz/', chr(5))
    url = url.replace('.gov/', chr(6))
    url = url.replace('.com', chr(7))
    url = url.replace('.org', chr(8))
    url = url.replace('.edu', chr(9))
    url = url.replace('.net', chr(10))
    url = url.replace('.info', chr(11))
    url = url.replace('.biz', chr(12))
    url = url.replace('.gov', chr(13))

    data = [
        0xAA, 0xFE, # Eddystone ID
        0x10, # Eddystone-URL
        tx_power #twocompl(power1m), # Power
    ]

    for c in url:
        data.append(ord(c))

    bluetooth.set_advertisement(service_uuid = bytes(uuid), service_data = bytes(data))

bluetooth.advertise(True)

while True:
    time.sleep(timeSleep)
    print("advertising")
