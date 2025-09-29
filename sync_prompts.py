# 同步 claude.md 和 agents.md 文件，用来保持 claude.md 和 agents.md 的内容一致。只要修改其中一个文件，脚本就会把内容同步到另一个。
# 使用：python3 sync_prompts.py

import os
import time

# 要同步的两个文件
FILES = ["claude.md", "agents.md"]

def get_mtime(path):
    return os.path.getmtime(path) if os.path.exists(path) else 0

def sync_files():
    mtimes = {f: get_mtime(f) for f in FILES}

    # 找出最近修改的文件
    latest_file = max(mtimes, key=mtimes.get)
    latest_mtime = mtimes[latest_file]

    # 如果最近修改时间为 0，说明两个文件都不存在
    if latest_mtime == 0:
        print("⚠️ 没有找到 claude.md 或 agents.md 文件")
        return

    # 读取最近修改的文件内容
    with open(latest_file, "r", encoding="utf-8") as f:
        content = f.read()

    # 把内容写入另一个文件
    for f in FILES:
        if f != latest_file:
            with open(f, "w", encoding="utf-8") as out:
                out.write(content)
            print(f"✅ 已将 {latest_file} 的内容同步到 {f}")

if __name__ == "__main__":
    sync_files()