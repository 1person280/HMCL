# 修复系统托盘菜单位置问题

## 问题分析
当前菜单显示位置不正确，需要根据托盘图标位置和任务栏位置来正确定位菜单。

## 解决方案
使用 MouseEvent 的屏幕坐标 (`getXOnScreen()`, `getYOnScreen()`) 来定位菜单，并根据屏幕边缘位置判断任务栏位置，调整菜单显示方向。

## 修改步骤

### 1. 修改 `SystemTrayManager.java` 中的 `showPopupMenu` 方法
- 获取屏幕尺寸
- 根据鼠标点击位置判断任务栏位置（顶部、底部、左侧、右侧）
- 调整菜单显示位置，确保菜单不会被截断

### 2. 具体实现逻辑
```
1. 获取屏幕尺寸: GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds()
2. 判断任务栏位置:
   - 如果点击位置 Y 接近屏幕底部 → 任务栏在底部，菜单向上弹出
   - 如果点击位置 Y 接近屏幕顶部 → 任务栏在顶部，菜单向下弹出
   - 如果点击位置 X 接近屏幕右侧 → 任务栏在右侧，菜单向左弹出
   - 如果点击位置 X 接近屏幕左侧 → 任务栏在左侧，菜单向右弹出
3. 计算菜单显示位置:
   - 使用 JPopupMenu.show() 方法，传入正确的父组件和坐标
   - 或使用 JPopupMenu.setLocation() 设置菜单位置
```

### 3. 代码修改
修改 `showPopupMenu(int x, int y)` 方法：
- 添加屏幕边界检测
- 根据托盘图标位置调整菜单弹出方向
- 确保菜单完全可见
