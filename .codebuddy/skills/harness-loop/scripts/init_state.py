#!/usr/bin/env python3
"""
Harness Loop - 初始化状态文件

用法: python init_state.py [--milestone M1]

读取 docs/plan/01-迭代计划.md 中的任务列表，
生成 docs/.harness-state.json 状态文件。
"""

import json
import os
import re
import sys
from datetime import datetime, timezone

STATE_FILE = "docs/.harness-state.json"
PLAN_FILE = "docs/plan/01-迭代计划.md"


def parse_plan(plan_path: str) -> dict:
    """解析迭代计划，提取各迭代的任务编号"""
    with open(plan_path, "r", encoding="utf-8") as f:
        content = f.read()

    milestones = {}
    current_milestone = None

    for line in content.split("\n"):
        # 匹配迭代标题，如 "## 1.2 M1 - 基础框架 + 博客核心"
        m = re.match(r"##\s+\d+\.\d+\s+(M\d+)\s*-\s*(.+)", line)
        if m:
            current_milestone = m.group(1)
            milestones[current_milestone] = {
                "name": m.group(2).strip(),
                "tasks": []
            }
            continue

        # 匹配任务行，如 "|| M1-01 | ..."
        if current_milestone:
            task_match = re.match(r"\|\|\s+(M\d+-\d+)\s+\|", line)
            if task_match:
                task_id = task_match.group(1)
                if task_id.startswith(current_milestone):
                    milestones[current_milestone]["tasks"].append(task_id)

    return milestones


def init_state(milestone: str = "M1") -> dict:
    """初始化状态文件"""
    plan_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(
        os.path.abspath(__file__))))), PLAN_FILE)

    if not os.path.exists(plan_path):
        print(f"⚠️  迭代计划文件不存在: {plan_path}")
        milestones = {milestone: {"name": "默认迭代", "tasks": []}}
    else:
        milestones = parse_plan(plan_path)

    if milestone not in milestones:
        print(f"⚠️  迭代 {milestone} 不在计划中，可用: {list(milestones.keys())}")
        tasks = []
    else:
        tasks = milestones[milestone]["tasks"]

    state = {
        "version": 1,
        "project": "personal-website",
        "current_phase": "require",
        "current_milestone": milestone,
        "current_task": tasks[0] if tasks else "",
        "completed_tasks": [],
        "phase_progress": {
            "require": {
                "status": "in_progress",
                "items": [],
                "confirmed_items": 0,
                "total_items": 0
            },
            "spec": {
                "status": "pending",
                "specs_generated": [],
                "specs_total": 0,
                "confirmed": False
            },
            "implement": {
                "status": "pending",
                "task_progress": {}
            },
            "verify": {
                "status": "pending",
                "results": [],
                "verified_items": 0,
                "total_items": 0
            }
        },
        "blocked_reason": "",
        "last_updated": datetime.now(timezone.utc).isoformat(),
        "history": []
    }

    return state


def main():
    milestone = "M1"
    if len(sys.argv) > 1:
        arg = sys.argv[1]
        if arg.startswith("--milestone="):
            milestone = arg.split("=", 1)[1]
        elif len(sys.argv) > 2 and sys.argv[1] == "--milestone":
            milestone = sys.argv[2]

    # 计算项目根目录 (从 scripts/ 上溯 4 层到 Base/)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(script_dir))))
    state_path = os.path.join(project_root, STATE_FILE)

    if os.path.exists(state_path):
        print(f"⚠️  状态文件已存在: {state_path}")
        with open(state_path, "r", encoding="utf-8") as f:
            existing = json.load(f)
        print(f"   当前阶段: {existing['current_phase']}")
        print(f"   当前迭代: {existing['current_milestone']}")
        print(f"   当前任务: {existing['current_task']}")
        print()
        answer = input("是否覆盖？(y/N): ").strip().lower()
        if answer != "y":
            print("已取消")
            return

    state = init_state(milestone)

    os.makedirs(os.path.dirname(state_path), exist_ok=True)
    with open(state_path, "w", encoding="utf-8") as f:
        json.dump(state, f, ensure_ascii=False, indent=2)

    print(f"✅ 状态文件已初始化: {state_path}")
    print(f"   迭代: {milestone}")
    print(f"   阶段: require")
    if state["current_task"]:
        print(f"   首个任务: {state['current_task']}")


if __name__ == "__main__":
    main()
