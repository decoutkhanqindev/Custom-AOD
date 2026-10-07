"""Sinh icon app (adaptive icon, khung 108): `ic_launcher_foreground.xml` và `ic_launcher_monochrome.xml`.

Chạy từ gốc repo: python tools/brand/generate_launcher_icon.py
Hình và màu lấy từ brand_geometry.py (dùng chung với Lottie của icon). Sửa ở đó rồi chạy lại, đừng sửa tay file XML.
"""
import io

from brand_geometry import (
    BORDER_STROKES, CAMERA_COLOR, CAMERA_RADIUS, CAMERA_X, CAMERA_Y, CLOCK_COLOR, CLOCK_GLOW_ALPHA, CLOCK_GLOW_WIDTH,
    DOT_COLOR, DOT_RADIUS, DOTS_X, DOTS_Y, EDGE_COLORS, MONOCHROME_COLOR, PHONE_H, PHONE_RADIUS, PHONE_W, PHONE_X,
    PHONE_Y, RES, SCREEN_COLOR, digit_origins, segment_boxes,
)

MONOCHROME_STROKE_WIDTH = 2.4
PATH_DATA_LIMIT = 800  # lint VectorPath


def fmt(v):
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return s if s else "0"


def rounded_rect(x, y, w, h, r):
    return (f"M{fmt(x + r)},{fmt(y)}H{fmt(x + w - r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x + w)},{fmt(y + r)}"
            f"V{fmt(y + h - r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x + w - r)},{fmt(y + h)}"
            f"H{fmt(x + r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x)},{fmt(y + h - r)}"
            f"V{fmt(y + r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x + r)},{fmt(y)}Z")


def pill(x, y, w, h):
    r = min(w, h) / 2
    if w >= h:
        return (f"M{fmt(x + r)},{fmt(y)}H{fmt(x + w - r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x + w - r)},{fmt(y + h)}"
                f"H{fmt(x + r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x + r)},{fmt(y)}Z")
    return (f"M{fmt(x)},{fmt(y + r)}A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x + w)},{fmt(y + r)}V{fmt(y + h - r)}"
            f"A{fmt(r)},{fmt(r)} 0,0 1,{fmt(x)},{fmt(y + h - r)}Z")


def circle(cx, cy, r):
    return (f"M{fmt(cx - r)},{fmt(cy)}A{fmt(r)},{fmt(r)} 0,1 0,{fmt(cx + r)},{fmt(cy)}"
            f"A{fmt(r)},{fmt(r)} 0,1 0,{fmt(cx - r)},{fmt(cy)}Z")


phone_path = rounded_rect(PHONE_X, PHONE_Y, PHONE_W, PHONE_H, PHONE_RADIUS)
camera_path = circle(CAMERA_X, CAMERA_Y, CAMERA_RADIUS)
digit_paths = ["".join(pill(*box) for box in segment_boxes(x0, y0, char)) for char, x0, y0 in digit_origins()]
dots_path = "".join(circle(cx, DOTS_Y, DOT_RADIUS) for cx in DOTS_X)
assert all(len(p) < PATH_DATA_LIMIT for p in digit_paths + [dots_path, phone_path])


def gradient():
    items = "".join(
        f'        <item android:offset="{fmt(i / (len(EDGE_COLORS) - 1))}" android:color="#FF{color}" />\n'
        for i, color in enumerate(EDGE_COLORS)
    )
    return f"""    <aapt:attr name="android:strokeColor">
      <gradient
          android:type="linear"
          android:startX="{fmt(PHONE_X)}"
          android:startY="{fmt(PHONE_Y)}"
          android:endX="{fmt(PHONE_X + PHONE_W)}"
          android:endY="{fmt(PHONE_Y + PHONE_H)}">
{items}      </gradient>
    </aapt:attr>
"""


def edge(stroke_width, alpha):
    return f"""  <path
      android:pathData="{phone_path}"
      android:strokeWidth="{fmt(stroke_width)}"
      android:strokeAlpha="{fmt(alpha)}">
{gradient()}  </path>
"""


# Lớp rộng, nhạt vẽ trước; lõi vẽ sau cùng nên nằm trên.
edges = "".join(edge(width, alpha) for width, alpha in reversed(BORDER_STROKES))
foreground = f"""<?xml version="1.0" encoding="utf-8"?>
<!-- Sinh bằng tools/brand/generate_launcher_icon.py: sửa script rồi chạy lại, đừng sửa tay.
     Icon app: điện thoại tắt màn hình có viền sáng (edge lighting) chuyển màu mint → xanh → tím, trên màn là đồng hồ
     xếp chồng kiểu AOD và ba chấm thông báo. Ba lớp viền cùng gradient, độ đậm khác nhau tạo quầng sáng.
     Mọi hình nằm trong vùng an toàn 66dp ở giữa khung 108. Cùng toạ độ với lottie_device_edge_light: splash của app phát
     Lottie đó và dùng drawable này làm placeholder lúc đang nạp, nên khớp splash hệ thống. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
  <path
      android:fillColor="#FF{SCREEN_COLOR}"
      android:pathData="{phone_path}" />
{edges}  <path
      android:fillColor="#FF{CAMERA_COLOR}"
      android:pathData="{camera_path}" />
""" + "".join(f"""  <path
      android:fillColor="#FF{CLOCK_COLOR}"
      android:strokeColor="#{CLOCK_GLOW_ALPHA:02X}{CLOCK_COLOR}"
      android:strokeWidth="{fmt(CLOCK_GLOW_WIDTH)}"
      android:pathData="{p}" />
""" for p in digit_paths) + f"""  <path
      android:fillColor="#FF{DOT_COLOR}"
      android:pathData="{dots_path}" />
</vector>
"""

monochrome = f"""<?xml version="1.0" encoding="utf-8"?>
<!-- Sinh bằng tools/brand/generate_launcher_icon.py: sửa script rồi chạy lại, đừng sửa tay.
     Lớp đơn sắc cho icon theo chủ đề (Android 13+): cùng hình với foreground, viền là nét đơn, không gradient, không quầng sáng. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
  <path
      android:strokeColor="#FF{MONOCHROME_COLOR}"
      android:strokeWidth="{fmt(MONOCHROME_STROKE_WIDTH)}"
      android:pathData="{phone_path}" />
""" + "".join(f"""  <path
      android:fillColor="#FF{MONOCHROME_COLOR}"
      android:pathData="{p}" />
""" for p in [camera_path] + digit_paths + [dots_path]) + "</vector>\n"

for name, body in (("ic_launcher_foreground.xml", foreground), ("ic_launcher_monochrome.xml", monochrome)):
    path = RES / "drawable" / name
    io.open(path, "w", encoding="utf-8", newline="\n").write(body)
    print("wrote", path.relative_to(RES.parents[3]), path.stat().st_size, "bytes")
