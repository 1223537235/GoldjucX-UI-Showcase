#!/bin/bash
# 从原项目同步 UIShowcaseScreen.kt（自动替换包名和 import）
SRC="/Users/gaojiaxiang/Downloads/手机管家_Demo/app/src/main/java/com/miui/peepguard/ui/screens/UIShowcaseScreen.kt"
DST="app/src/main/java/com/goldjucx/showcase/ui/screens/UIShowcaseScreen.kt"

sed \
  -e 's/package com.miui.peepguard.ui.screens/package com.goldjucx.showcase.ui.screens/' \
  -e 's/import com.miui.peepguard.R/import com.goldjucx.showcase.R/' \
  -e 's/import com.miui.peepguard.ui.theme.OnSurfaceQuaternary/import com.goldjucx.showcase.ui.theme.OnSurfaceQuaternary/' \
  "$SRC" > "$DST"

# 同步 drawable 资源
rsync -a --delete \
  --include='ic_*.xml' --include='illustration_*' --include='bg_*' --exclude='*' \
  "/Users/gaojiaxiang/Downloads/手机管家_Demo/app/src/main/res/drawable/" \
  "app/src/main/res/drawable/"

# 同步 strings
cp "/Users/gaojiaxiang/Downloads/手机管家_Demo/app/src/main/res/values/strings_ui_showcase.xml" \
   "app/src/main/res/values/strings_ui_showcase.xml"

echo "✓ 同步完成"
