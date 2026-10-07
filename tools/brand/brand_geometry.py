"""Hình học và màu dùng chung cho icon app và animation Lottie của icon.

Icon (`ic_launcher_foreground.xml`) và Lottie (`lottie_device_edge_light.json`) phải cùng toạ độ trong khung 108 của
adaptive icon: splash hệ thống vẽ icon tĩnh, splash của app phát Lottie đúng chỗ đó với cùng cỡ, khung đầu trùng nhau
thì lúc chuyển màn không nhảy. Đổi hình hay màu ở đây rồi chạy lại cả hai script sinh.
"""
from pathlib import Path

RES = Path(__file__).resolve().parents[2] / "app" / "src" / "main" / "res"

# Màu hex (trùng AodsColors). Viền sáng chuyển màu từ góc trên trái xuống góc dưới phải của điện thoại.
MINT, BLUE, PURPLE = "7FD1AE", "8AB4F8", "C3A6FF"
EDGE_COLORS = (MINT, BLUE, PURPLE)
CLOCK_COLOR = MINT
DOT_COLOR, CAMERA_COLOR, SCREEN_COLOR, MONOCHROME_COLOR = "9A9A9A", "5A5A5A", "000000", "FFFFFF"

# Điện thoại trong khung 108; mọi hình phải nằm trong vùng an toàn 66 ở giữa (bán kính 33 quanh tâm 54, 54).
PHONE_X, PHONE_Y, PHONE_W, PHONE_H, PHONE_RADIUS = 38.0, 26.0, 32.0, 56.0, 7.5
CAMERA_X, CAMERA_Y, CAMERA_RADIUS = 54, 30.5, 1.0
DOTS_Y, DOT_RADIUS, DOTS_X = 75.5, 1.1, (50.5, 54, 57.5)

# Viền tĩnh: ba lớp cùng dải màu (độ rộng, độ đậm 0..1) — lõi và quầng sáng.
BORDER_STROKES = [(1.6, 1.0), (3.2, 0.4), (6, 0.18)]

# Đồng hồ 7 đoạn xếp chồng: mỗi dòng hai chữ số; đoạn là thanh bo tròn hai đầu.
CLOCK_ROWS = ("08", "30")
DIGIT_W, DIGIT_H, SEGMENT_T, SEGMENT_GAP = 9.6, 15.5, 2.4, 0.5
COLUMN_GAP, ROW_GAP, CLOCK_TOP = 3.2, 3.0, 36.0
CLOCK_GLOW_WIDTH, CLOCK_GLOW_ALPHA = 1.8, 0x40
SEGMENTS = {
    "0": "abcdef", "1": "bc", "2": "abged", "3": "abcdg", "4": "fgbc",
    "5": "afgcd", "6": "afgedc", "7": "abc", "8": "abcdefg", "9": "abcdfg",
}


def phone_center():
    return PHONE_X + PHONE_W / 2, PHONE_Y + PHONE_H / 2


def digit_origins():
    """Góc trên trái của từng chữ số, theo thứ tự dòng rồi cột."""
    left = 54 - (2 * DIGIT_W + COLUMN_GAP) / 2
    origins = []
    for row, text in enumerate(CLOCK_ROWS):
        y0 = CLOCK_TOP + row * (DIGIT_H + ROW_GAP)
        for column, char in enumerate(text):
            origins.append((char, left + column * (DIGIT_W + COLUMN_GAP), y0))
    return origins


def segment_boxes(x0, y0, char):
    """Các đoạn sáng của một chữ số: (x, y, rộng, cao) của từng thanh."""
    radius = SEGMENT_T / 2
    hx, hw = x0 + radius + SEGMENT_GAP, DIGIT_W - 2 * (radius + SEGMENT_GAP)
    vh = DIGIT_H / 2 - SEGMENT_GAP - (radius + SEGMENT_GAP)
    boxes = {
        "a": (hx, y0, hw, SEGMENT_T),
        "g": (hx, y0 + DIGIT_H / 2 - radius, hw, SEGMENT_T),
        "d": (hx, y0 + DIGIT_H - SEGMENT_T, hw, SEGMENT_T),
        "f": (x0, y0 + radius + SEGMENT_GAP, SEGMENT_T, vh),
        "b": (x0 + DIGIT_W - SEGMENT_T, y0 + radius + SEGMENT_GAP, SEGMENT_T, vh),
        "e": (x0, y0 + DIGIT_H / 2 + SEGMENT_GAP, SEGMENT_T, vh),
        "c": (x0 + DIGIT_W - SEGMENT_T, y0 + DIGIT_H / 2 + SEGMENT_GAP, SEGMENT_T, vh),
    }
    return [boxes[segment] for segment in SEGMENTS[char]]


def hex_to_rgb01(color, lighten=0.0):
    """Màu hex → [r, g, b] trong 0..1, pha trắng theo `lighten`."""
    channels = [int(color[i:i + 2], 16) for i in (0, 2, 4)]
    return [round((v + (255 - v) * lighten) / 255, 4) for v in channels]
