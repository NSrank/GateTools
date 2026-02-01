# GateTools - 传送门插件

一个功能强大的Minecraft Paper服务器传送门插件，支持区域传送和权限管理。  
> **注意**：本插件由 AI 辅助开发

## 功能特性

- **区域传送**: 通过设定矩形区域创建传送门
- **交互式确认**: 玩家进入传送区域时显示确认界面
- **条件系统**: 支持经验、权限、金钱等传送条件
- **权限管理**: 完整的权限节点系统
- **数据持久化**: 自动保存和手动保存功能
- **Vault集成**: 支持经济系统集成

## 服务器要求

- **服务器**: Paper 1.20.1
- **Java版本**: Java 17+
- **可选依赖**: Vault（用于经济功能）

## 安装方法

1. 下载 `GateTools-1.1.jar` 文件
2. 将文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 插件将自动生成配置文件

## 命令使用

### 基础命令
- `/gatetools help` - 显示帮助信息
- `/gatetools reload` - 重载配置文件
- `/gatetools save` - 手动保存数据

### 传送门管理
- `/gatetools set <角点1> <角点2> <配置名> <显示名>` - 创建传送区域
- `/gatetools edit <配置名> <配置项> [判断器] [值]` - 编辑传送区域
- `/gatetools delete <配置名>` - 删除传送区域
- `/gatetools confirm` - 确认删除操作

**注意**: teleport配置项直接接坐标，不需要判断器

### 命令示例

#### 创建传送门
```
/gatetools set world,100,64,200 world,110,74,210 gate1 传送门1
```

#### 设置传送目标
```
/gatetools edit gate1 teleport world,300,64,400
```

#### 设置权限要求
```
/gatetools edit gate1 permission set = teleport.gate1
```

#### 设置金钱费用
```
/gatetools edit gate1 money cost 100
```

#### 设置经验要求
```
/gatetools edit gate1 experience set >= 10
```

## 权限节点

### 管理权限
- `gatetools.command` - 使用基础命令
- `gatetools.command.set` - 创建传送区域
- `gatetools.command.edit` - 编辑传送区域
- `gatetools.command.delete` - 删除传送区域
- `gatetools.command.reload` - 重载配置
- `gatetools.command.save` - 保存数据

### 使用权限
- `gatetools.use` - 使用传送门（默认为true）

## 配置文件

### config.yml
主配置文件，包含插件设置、消息配置等。

### data.yml
数据文件，存储所有传送门信息（自动生成）。

## 故障排除

如果传送功能不工作，请按以下步骤检查：

1. **启用调试模式**: 在config.yml中设置`debug: true`
2. **检查传送目标**: 确保使用正确的命令格式设置传送目标
   ```
   /gatetools edit <传送门名> teleport <世界名>,<x>,<y>,<z>
   ```
3. **检查权限**: 确保玩家有`gatetools.use`权限
4. **查看控制台**: 调试模式下会输出详细的日志信息
5. **重载插件**: 使用`/gatetools reload`重载配置

## 条件系统

### 支持的配置项
- `experience` - 经验等级
- `permission` - 权限节点
- `money` - 金钱（需要Vault）
- `teleport` - 传送目标

### 判断器类型
- `set` - 设定判断条件
- `cost` - 设定花费（仅experience、money可用）

### 比较操作符
- `=` - 等于
- `!` - 不等于
- `>` - 大于（仅数值类型）
- `>=` - 大于等于（仅数值类型）
- `<` - 小于（仅数值类型）
- `<=` - 小于等于（仅数值类型）

## 开发信息

- **作者**: NSrank, Augment
- **版本**: 1.1
- **许可证**: MIT
- **源码**: https://github.com/NSrank/GateTools

## 更新日志

### v1.1
- 修复消息配置重复问题
- 将所有自定义消息统一到messages.yml文件
- 移除config.yml中的重复消息配置
- 优化消息管理系统

### v1.0
- 初始版本发布
- 实现基础传送门功能
- 支持条件系统
- 集成Vault经济系统
- 完整的权限管理

## 支持

如果您遇到问题或有建议，请在GitHub上提交Issue。

---

**English Version Below**

# GateTools - Teleportation Gate Plugin

A powerful Minecraft Paper server teleportation gate plugin with region-based teleportation and permission management.

## Features

- **Region Teleportation**: Create teleportation gates using rectangular regions
- **Interactive Confirmation**: Display confirmation interface when players enter teleportation areas
- **Condition System**: Support for experience, permission, money and other teleportation conditions
- **Permission Management**: Complete permission node system
- **Data Persistence**: Auto-save and manual save functionality
- **Vault Integration**: Economy system integration support

## Server Requirements

- **Server**: Paper 1.20.1
- **Java Version**: Java 17+
- **Optional Dependencies**: Vault (for economy features)

## Installation

1. Download the `GateTools-1.1.jar` file
2. Place the file in your server's `plugins` folder
3. Restart the server
4. The plugin will automatically generate configuration files

## Commands

### Basic Commands
- `/gatetools help` - Show help information
- `/gatetools reload` - Reload configuration files
- `/gatetools save` - Manually save data

### Gate Management
- `/gatetools set <corner1> <corner2> <config_name> <display_name>` - Create teleportation area
- `/gatetools edit <config_name> <config_item> <judge_type> [value]` - Edit teleportation area
- `/gatetools delete <config_name>` - Delete teleportation area
- `/gatetools confirm` - Confirm deletion operation

## Permissions

### Administrative Permissions
- `gatetools.command` - Use basic commands
- `gatetools.command.set` - Create teleportation areas
- `gatetools.command.edit` - Edit teleportation areas
- `gatetools.command.delete` - Delete teleportation areas
- `gatetools.command.reload` - Reload configuration
- `gatetools.command.save` - Save data

### Usage Permissions
- `gatetools.use` - Use teleportation gates (default: true)

## Development Information

- **Authors**: NSrank, Augment
- **Version**: 1.1
- **License**: MIT
- **Source Code**: https://github.com/NSrank/GateTools
