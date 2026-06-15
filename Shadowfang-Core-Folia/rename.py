import os

directory = r'F:\Shadowfang-Folia-Project\Shadowfang-Core-Folia\src\main\java\com\shadowfang\core'

for root, dirs, files in os.walk(directory):
    for filename in files:
        if filename.endswith(".java"):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            content = content.replace('com.shadowfang.bitterroot', 'com.shadowfang.core')
            content = content.replace('ShadowfangBitterrootPlugin', 'ShadowfangCorePlugin')
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)

print("Renaming complete!")
