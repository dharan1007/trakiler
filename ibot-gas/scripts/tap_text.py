import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

TARGET = sys.argv[1]
REMOTE = "/sdcard/window.xml"
LOCAL = "/tmp/window.xml"

subprocess.run(["adb", "shell", "uiautomator", "dump", REMOTE], check=True, stdout=subprocess.DEVNULL)
subprocess.run(["adb", "pull", REMOTE, LOCAL], check=True, stdout=subprocess.DEVNULL)

root = ET.parse(LOCAL).getroot()
match = None
for node in root.iter("node"):
    if node.attrib.get("text") == TARGET or node.attrib.get("content-desc") == TARGET or node.attrib.get("content-desc") == TARGET + " tab":
        match = node
        break

if match is None:
    raise SystemExit("UI element not found: " + TARGET)

m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", match.attrib.get("bounds", ""))
if not m:
    raise SystemExit("Invalid bounds for: " + TARGET)

x1, y1, x2, y2 = map(int, m.groups())
subprocess.run(["adb", "shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2)], check=True)
time.sleep(1)
