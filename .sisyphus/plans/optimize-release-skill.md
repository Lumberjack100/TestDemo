# 优化 Release 技能 - 支持预览版和正式版发布

## Context

### Original Request
优化 `.claude/skills/release` 技能，同时支持发布预览版和正式版：

**预览版发布**：
- 对应分支: `fea_next`
- 更新文件: `CHANGELOG_PREVIEW.md`
- Tag 格式: `Tag_D_V<version>`

**正式版发布**：
- 对应分支: `develop_v5.0`
- 如果当前在 `fea_next` 分支，需先切换到 `develop_v5.0` 并合并 `fea_next`
- 切换分支和合并操作必须经过用户确认
- 更新文件: `CHANGELOG.md`
- Tag 格式: `Tag_R_V<version>`

### Interview Summary
**Key Discussions**:
- 用户需求明确，无需额外讨论
- 项目已有两个 Changelog 文件：`CHANGELOG.md` 和 `CHANGELOG_PREVIEW.md`
- 当前分支状态：`fea_next` (当前)，`develop_v5.0` (正式版分支)

**Research Findings**:
- 现有 `SKILL.md` 文件结构完整，包含 11 个步骤的工作流程
- 需要在开头添加发布类型选择步骤
- 需要添加分支检查和切换逻辑
- Tag 格式需从 `V<version>` 改为 `Tag_D_V<version>` 或 `Tag_R_V<version>`

---

## Work Objectives

### Core Objective
重写 `.claude/skills/release/SKILL.md` 文件，使其支持预览版和正式版两种发布模式，包含分支管理、不同的 Changelog 文件和不同的 Tag 格式。

### Concrete Deliverables
- 更新后的 `.claude/skills/release/SKILL.md` 文件

### Definition of Done
- [ ] 技能文档包含发布类型选择步骤（预览版/正式版）
- [ ] 技能文档包含分支检查和切换逻辑
- [ ] 正式版发布时，从 `fea_next` 合并需要用户确认
- [ ] 预览版使用 `CHANGELOG_PREVIEW.md` 和 `Tag_D_V<version>` 格式
- [ ] 正式版使用 `CHANGELOG.md` 和 `Tag_R_V<version>` 格式
- [ ] 权限配置包含新增的 git 命令（checkout, pull, merge）

### Must Have
- 发布类型选择交互（使用 question 工具）
- 分支切换确认交互（使用 question 工具）
- 预览版/正式版的完整工作流程说明
- 完整示例（预览版发布示例和正式版发布示例）

### Must NOT Have (Guardrails)
- 不要改变版本号推荐逻辑（patch/minor/major）
- 不要删除现有的故障排除章节
- 不要修改约定式提交的分类规则

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (技能文档，无自动化测试)
- **User wants tests**: Manual-only
- **Framework**: none

### Manual QA Only

**验证方法**: 人工审查文档内容

---

## Task Flow

```
Task 1 (编写新 SKILL.md 内容)
```

## Parallelization

| Task | Depends On | Reason |
|------|------------|--------|
| 1 | None | 单一任务，无依赖 |

---

## TODOs

- [ ] 1. 重写 Release 技能文档

  **What to do**:
  使用 `Write` 工具，将以下完整内容写入 `.claude/skills/release/SKILL.md`：

  ```markdown
  ---
  name: release
  description: 自动化软件版本发布流程，支持预览版和正式版发布，包括 changelog 更新、Git 提交、Tag 创建和推送
  triggerKeywords:
    - release
    - 发布版本
    - 发布预览版
    - 发布正式版
    - 创建 release
    - 更新 changelog
    - 打 tag
  ---

  # Release 技能

  ## 概述

  Release 技能用于自动化软件版本发布流程。**无需外部脚本**，AI Agent 直接执行所有步骤。

  本技能支持两种发布模式：

  | 发布类型 | 分支 | Changelog 文件 | Tag 格式 |
  |----------|------|----------------|----------|
  | **预览版 (Preview)** | `fea_next` | `CHANGELOG_PREVIEW.md` | `Tag_D_V<version>` |
  | **正式版 (Production)** | `develop_v5.0` | `CHANGELOG.md` | `Tag_R_V<version>` |

  ## 工作流程

  当用户触发发布流程时，按以下步骤执行：

  ### Step 0: 选择发布类型

  **首先使用 `question` 工具让用户选择发布类型**：

  ```json
  {
    "questions": [{
      "question": "请选择发布类型",
      "header": "发布类型",
      "options": [
        {"label": "预览版 (Preview)", "description": "发布到 fea_next 分支，更新 CHANGELOG_PREVIEW.md，Tag 格式: Tag_D_V<version>"},
        {"label": "正式版 (Production)", "description": "发布到 develop_v5.0 分支，更新 CHANGELOG.md，Tag 格式: Tag_R_V<version>"}
      ]
    }]
  }
  ```

  **根据用户选择设置以下变量**：

  | 用户选择 | `TARGET_BRANCH` | `CHANGELOG_FILE` | `TAG_PREFIX` |
  |----------|-----------------|------------------|--------------|
  | 预览版 | `fea_next` | `CHANGELOG_PREVIEW.md` | `Tag_D_V` |
  | 正式版 | `develop_v5.0` | `CHANGELOG.md` | `Tag_R_V` |

  ### Step 1: 检查并切换分支

  ```bash
  # 获取当前分支
  git branch --show-current
  ```

  **分支处理逻辑**：

  #### 如果发布预览版：
  - 当前分支必须是 `fea_next`
  - 如果不在 `fea_next`，提示用户需要先切换到 `fea_next` 分支

  #### 如果发布正式版：
  - 目标分支是 `develop_v5.0`
  - **如果当前在 `fea_next` 分支**，需要：
    1. **使用 `question` 工具确认分支切换和合并操作**：
    
    ```json
    {
      "questions": [{
        "question": "发布正式版需要切换到 develop_v5.0 分支并合并 fea_next 的更改。是否继续？",
        "header": "分支操作确认",
        "options": [
          {"label": "是，切换并合并", "description": "切换到 develop_v5.0 并合并 fea_next 分支"},
          {"label": "否，取消发布", "description": "取消本次发布操作"}
        ]
      }]
    }
    ```
    
    2. **如果用户确认**，执行以下命令：
    
    ```bash
    # 切换到 develop_v5.0 分支
    git checkout develop_v5.0
    
    # 拉取最新代码
    git pull origin develop_v5.0
    
    # 合并 fea_next 分支
    git merge fea_next
    ```
    
    3. 如果合并出现冲突，提示用户手动解决冲突后再继续
    
    4. **如果用户取消**，终止发布流程

  - **如果当前已在 `develop_v5.0` 分支**，直接继续

  ### Step 2: 读取当前版本号

  从对应的 Changelog 文件读取当前版本号：

  ```bash
  # 获取第一个版本号（格式：## x.y.z）
  head -20 ${CHANGELOG_FILE}
  ```

  从输出中提取形如 `## 5.5.11` 的版本号，解析为 `major.minor.patch` 格式。

  ### Step 3: 获取最新 Tag

  ```bash
  # 根据发布类型获取对应格式的 Tag
  # 预览版: Tag_D_V*
  # 正式版: Tag_R_V*
  git tag -l "${TAG_PREFIX}*" --sort=-v:refname | head -5
  ```

  如果存在 Tag，记录最新的 Tag（如 `Tag_D_V5.5.11` 或 `Tag_R_V5.5.5`）。

  ### Step 4: 获取提交日志

  ```bash
  # 如果有 Tag，获取从 Tag 到 HEAD 的提交
  git log --no-merges --pretty=format:"%s" ${TAG_PREFIX}<version>..HEAD

  # 如果没有 Tag，获取所有提交
  git log --no-merges --pretty=format:"%s"
  ```

  ### Step 5: 分类提交

  根据约定式提交前缀对提交进行分类：

  | 前缀 | 分类 | 版本影响 |
  |------|------|----------|
  | `feat:` | Features | 建议 minor 升级 |
  | `fix:` | Bug Fixes | 建议 patch 升级 |
  | `docs:` | Documentation | patch 升级 |
  | `chore:` | Chore | patch 升级 |
  | `refactor:` | Refactoring | patch 升级 |
  | `test:` | Tests | patch 升级 |
  | `BREAKING CHANGE` 或 `!:` | Breaking | 建议 major 升级 |
  | 其他 | Other Changes | patch 升级 |

  ### Step 6: 智能版本号推荐 (交互确认)

  **根据提交类型分析推荐版本号**：

  - 如果存在 `BREAKING CHANGE` 或 `feat!:` → 推荐 **major** 升级
  - 如果存在 `feat:` 提交 → 推荐 **minor** 升级
  - 其他情况 → 推荐 **patch** 升级

  **必须使用 `question` 工具让用户选择版本号**：

  假设当前版本为 `5.5.11`，根据分析结果：

  ```
  推荐版本号: 5.5.12 (patch)

  可选版本:
  - 5.5.12 (patch) - 仅 Bug 修复和小改动 [推荐]
  - 5.6.0 (minor) - 新增功能，向后兼容
  - 6.0.0 (major) - 重大变更，可能不兼容
  - 自定义版本号 - 手动输入任意版本号
  ```

  **question 工具调用示例**：

  ```json
  {
    "questions": [{
      "question": "请选择新版本号。当前版本: 5.5.11，检测到 N 个 feat 提交、M 个 fix 提交。",
      "header": "选择版本号",
      "options": [
        {"label": "5.5.12 (patch) (Recommended)", "description": "仅 Bug 修复和小改动"},
        {"label": "5.6.0 (minor)", "description": "新增功能，向后兼容"},
        {"label": "6.0.0 (major)", "description": "重大变更，可能不兼容"},
        {"label": "自定义版本号", "description": "手动输入任意版本号（如 5.5.12-beta.1）"}
      ]
    }]
  }
  ```

  **等待用户选择后再继续**。

  **如果用户选择「自定义版本号」**：

  使用 `question` 工具的文本输入模式让用户输入版本号：

  ```json
  {
    "questions": [{
      "question": "请输入自定义版本号（格式如: 5.5.12 或 5.5.12-beta.1）",
      "header": "输入版本号",
      "allowsMultipleSelection": false
    }]
  }
  ```

  **版本号格式校验**：
  - 必须符合语义化版本规范（SemVer）
  - 基本格式：`major.minor.patch`（如 `5.5.12`）
  - 可选预发布标识：`major.minor.patch-prerelease`（如 `5.5.12-alpha.1`、`6.0.0-rc.1`）
  - 如果格式不正确，提示用户重新输入

  ### Step 7: 生成 Changelog 条目

  根据用户选择的版本号，生成 Markdown 格式的 changelog 条目：

  ```markdown
  ## <new_version>

  ### Features
  - feat(scope): 描述
  - feat: 描述

  ### Bug Fixes
  - fix: 描述

  ### Documentation
  - docs: 描述

  ### Other Changes
  - chore: 描述
  ```

  **注意**：只输出有内容的分类，空分类不输出。

  ### Step 8: 更新 Changelog 文件

  将新生成的条目**插入到文件开头**（在原有内容之前）。

  使用 `Write` 或 `Edit` 工具更新对应的 Changelog 文件：
  - 预览版: `CHANGELOG_PREVIEW.md`
  - 正式版: `CHANGELOG.md`

  ### Step 9: 提交 Changelog

  ```bash
  git add ${CHANGELOG_FILE}
  git commit -m "chore: update changelog for version <new_version>"
  ```

  ### Step 10: 创建 Tag

  ```bash
  # 创建 annotated Tag
  # 预览版格式: Tag_D_V<new_version>
  # 正式版格式: Tag_R_V<new_version>
  git tag -a ${TAG_PREFIX}<new_version> -m "Release version <new_version>"
  ```

  **Tag 格式说明**：
  - 预览版: `Tag_D_V5.5.12` (D = Development/Debug)
  - 正式版: `Tag_R_V5.5.6` (R = Release)

  ### Step 11: 确认并推送到远程仓库 (交互确认)

  **必须使用 `question` 工具让用户确认是否推送**：

  ```json
  {
    "questions": [{
      "question": "Tag ${TAG_PREFIX}<new_version> 已创建。是否推送代码和 Tag 到远程仓库？",
      "header": "推送确认",
      "options": [
        {"label": "是，推送代码和 Tag", "description": "执行 git push && git push origin ${TAG_PREFIX}<new_version>"},
        {"label": "仅推送 Tag", "description": "仅执行 git push origin ${TAG_PREFIX}<new_version>"},
        {"label": "否，稍后手动推送", "description": "跳过推送，保留本地 Tag"}
      ]
    }]
  }
  ```

  **等待用户确认后执行相应操作**：

  **如果用户选择「是，推送代码和 Tag」**：

  ```bash
  # 推送代码到远程
  git push

  # 推送 Tag 到远程
  git push origin ${TAG_PREFIX}<new_version>
  ```

  **如果用户选择「仅推送 Tag」**：

  ```bash
  # 仅推送 Tag 到远程
  git push origin ${TAG_PREFIX}<new_version>
  ```

  **如果用户选择「否，稍后手动推送」**：

  输出提示信息：

  ```
  Tag ${TAG_PREFIX}<new_version> 已在本地创建。
  稍后可使用以下命令手动推送：
    git push                                    # 推送代码
    git push origin ${TAG_PREFIX}<new_version>  # 推送 Tag
  ```

  ## 完整示例

  ### 示例 1: 发布预览版

  用户说："发布预览版" 或 "release preview"

  AI Agent 执行：

  1. **询问发布类型** → 用户选择「预览版」
  2. 检查分支 → 确认在 `fea_next`
  3. 读取 `CHANGELOG_PREVIEW.md` → 当前版本 `5.5.11`
  4. 获取最新 Tag → `Tag_D_V5.5.11`
  5. 获取提交日志 → 发现 2 个 feat、3 个 fix
  6. 分类提交 → Features: 2, Bug Fixes: 3
  7. **询问用户选择版本号** → 用户选择 `5.5.12`
  8. 生成 changelog 条目
  9. 更新 `CHANGELOG_PREVIEW.md`
  10. 提交: `git commit -m "chore: update changelog for version 5.5.12"`
  11. 创建 Tag: `git tag -a Tag_D_V5.5.12 -m "Release version 5.5.12"`
  12. **询问用户是否推送** → 用户确认后执行推送
  13. 推送: `git push && git push origin Tag_D_V5.5.12`

  ### 示例 2: 发布正式版（从 fea_next 合并）

  用户说："发布正式版" 或 "release production"

  AI Agent 执行：

  1. **询问发布类型** → 用户选择「正式版」
  2. 检查分支 → 当前在 `fea_next`
  3. **询问是否切换并合并** → 用户确认
  4. 切换分支: `git checkout develop_v5.0`
  5. 拉取最新: `git pull origin develop_v5.0`
  6. 合并分支: `git merge fea_next`
  7. 读取 `CHANGELOG.md` → 当前版本 `5.5.5`
  8. 获取最新 Tag → `Tag_R_V5.5.5`
  9. 获取提交日志 → 发现 5 个 feat、8 个 fix
  10. 分类提交 → Features: 5, Bug Fixes: 8
  11. **询问用户选择版本号** → 用户选择 `5.6.0`
  12. 生成 changelog 条目
  13. 更新 `CHANGELOG.md`
  14. 提交: `git commit -m "chore: update changelog for version 5.6.0"`
  15. 创建 Tag: `git tag -a Tag_R_V5.6.0 -m "Release version 5.6.0"`
  16. **询问用户是否推送** → 用户确认后执行推送
  17. 推送: `git push && git push origin Tag_R_V5.6.0`

  ## 故障排除

  ### 未找到 Changelog 文件

  创建初始版本：

  **预览版 (CHANGELOG_PREVIEW.md)**:
  ```markdown
  ## 5.5.0

  ### Features
  - feat: 初始预览版本
  ```

  **正式版 (CHANGELOG.md)**:
  ```markdown
  ## 5.5.0

  ### Features
  - feat: 初始版本
  ```

  ### 没有 Git Tag

  首次发布时会获取所有提交记录。发布后会自动创建首个 Tag。

  ### 分支切换冲突

  如果从 `fea_next` 合并到 `develop_v5.0` 时出现冲突：

  1. 手动解决冲突文件
  2. 执行 `git add .`
  3. 执行 `git commit -m "merge: resolve conflicts from fea_next"`
  4. 重新触发发布流程

  ### 提交未按预期分类

  确保提交消息遵循约定式提交格式：

  ```
  <type>(<scope>): <description>

  # 示例
  feat(skills): 新增 release 技能
  fix: 修复版本号递增逻辑
  docs: 更新 README
  ```

  ## 权限配置

  在 `.claude/settings.local.json` 中添加以下权限：

  ```json
  {
    "permissions": {
      "allow": [
        "Skill(release)",
        "Bash(git log:*)",
        "Bash(git tag:*)",
        "Bash(git commit:*)",
        "Bash(git push:*)",
        "Bash(git add:*)",
        "Bash(git rev-parse:*)",
        "Bash(git branch:*)",
        "Bash(git checkout:*)",
        "Bash(git pull:*)",
        "Bash(git merge:*)",
        "Read(CHANGELOG.md)",
        "Read(CHANGELOG_PREVIEW.md)",
        "Write(CHANGELOG.md)",
        "Write(CHANGELOG_PREVIEW.md)"
      ]
    }
  }
  ```

  ## 快速参考

  | 操作 | 预览版 | 正式版 |
  |------|--------|--------|
  | 触发词 | `发布预览版`, `release preview` | `发布正式版`, `release production` |
  | 目标分支 | `fea_next` | `develop_v5.0` |
  | Changelog 文件 | `CHANGELOG_PREVIEW.md` | `CHANGELOG.md` |
  | Tag 格式 | `Tag_D_V<version>` | `Tag_R_V<version>` |
  | 需要合并 | 否 | 如果在 `fea_next`，需要先合并 |
  ```

  **Must NOT do**:
  - 不要修改其他文件
  - 不要添加不必要的内容

  **Parallelizable**: NO (单一任务)

  **References**:

  **Pattern References**:
  - `.claude/skills/release/SKILL.md` - 现有技能文档结构

  **API/Type References**:
  - 无

  **Documentation References**:
  - `CHANGELOG.md` - 正式版 changelog 文件格式参考
  - `CHANGELOG_PREVIEW.md` - 预览版 changelog 文件格式参考

  **WHY Each Reference Matters**:
  - 现有 SKILL.md 是重写的模板，保留其结构和交互模式
  - Changelog 文件展示了版本号格式（`## x.y.z`）

  **Acceptance Criteria**:

  **Manual Execution Verification**:
  - [ ] 检查文件是否成功写入
  - [ ] 检查文档结构是否完整（包含 Step 0 到 Step 11）
  - [ ] 检查发布类型选择交互是否正确
  - [ ] 检查分支切换确认交互是否正确
  - [ ] 检查权限配置是否包含新增的 git 命令
  - [ ] 检查快速参考表格是否正确

  **Commit**: YES
  - Message: `feat(skills): 优化 release 技能支持预览版和正式版发布`
  - Files: `.claude/skills/release/SKILL.md`
  - Pre-commit: 无

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(skills): 优化 release 技能支持预览版和正式版发布` | `.claude/skills/release/SKILL.md` | 人工审查 |

---

## Success Criteria

### Verification Commands
```bash
# 检查文件是否存在
cat .claude/skills/release/SKILL.md | head -50

# 检查关键内容是否包含
grep -c "预览版" .claude/skills/release/SKILL.md  # 应返回 > 0
grep -c "正式版" .claude/skills/release/SKILL.md  # 应返回 > 0
grep -c "Tag_D_V" .claude/skills/release/SKILL.md  # 应返回 > 0
grep -c "Tag_R_V" .claude/skills/release/SKILL.md  # 应返回 > 0
grep -c "fea_next" .claude/skills/release/SKILL.md  # 应返回 > 0
grep -c "develop_v5.0" .claude/skills/release/SKILL.md  # 应返回 > 0
```

### Final Checklist
- [ ] 文档包含发布类型选择（Step 0）
- [ ] 文档包含分支检查和切换逻辑（Step 1）
- [ ] 预览版配置正确（fea_next, CHANGELOG_PREVIEW.md, Tag_D_V）
- [ ] 正式版配置正确（develop_v5.0, CHANGELOG.md, Tag_R_V）
- [ ] 分支切换需要用户确认
- [ ] 权限配置完整
- [ ] 完整示例覆盖两种发布场景
