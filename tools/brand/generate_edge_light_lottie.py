"""Sinh Lottie của icon: `res/raw/lottie_device_edge_light.json` — đèn LED chạy theo viền máy, aura "thở" phía sau.

Chạy từ gốc repo: python tools/brand/generate_edge_light_lottie.py
Hình và màu lấy từ brand_geometry.py (cùng toạ độ với icon nên khung đầu trùng icon tĩnh). Các thông số chuyển động ở
ngay dưới đây. Không dùng hiệu ứng Gaussian Blur của Lottie: lottie-android vẽ nó theo px màn hình, không co giãn theo
cỡ animation; độ mờ / quầng sáng làm bằng nhiều lớp nét rộng dần, nhạt dần.
"""
import io
import json

from brand_geometry import (
    BORDER_STROKES, CAMERA_COLOR, CAMERA_RADIUS, CAMERA_X, CAMERA_Y, CLOCK_COLOR, CLOCK_GLOW_ALPHA, CLOCK_GLOW_WIDTH,
    DOT_COLOR, DOT_RADIUS, DOTS_X, DOTS_Y, EDGE_COLORS, PHONE_H, PHONE_RADIUS, PHONE_W, PHONE_X, PHONE_Y, RES,
    SCREEN_COLOR, digit_origins, hex_to_rgb01, phone_center, segment_boxes,
)

OUT = RES / "raw" / "lottie_device_edge_light.json"

FPS, FRAMES = 60, 144  # một vòng viền mỗi FRAMES / FPS giây (2,4 giây)

# Đèn LED: mỗi lớp là một đoạn rất nhạt cùng đỉnh PEAK (% chu vi); đuôi dài dần theo TAIL_CURVE (sáng tập trung gần đỉnh,
# đuôi mờ dài), mép trước dài dần đều. Nhiều lớp mỏng chồng lên nhau thành dốc sáng mịn; đầu nét phẳng để không thành cục.
PEAK = 42.0
TAIL_CURVE = 1.6
LED_LAYERS = [  # (độ rộng, số lớp, độ đậm mỗi lớp %, pha trắng 0..1, đuôi %, mép trước %)
    (1.8, 24, 8, 0.4, 34.0, 7.0),
    (4.0, 12, 2.6, 0.1, 38.0, 9.0),
    (7.0, 8, 1.7, 0.0, 40.0, 11.0),
]

# Aura sau điện thoại: vòng gradient tròn, co giãn và sáng / mờ theo nhịp một vòng viền.
AURA_DIAMETER, AURA_RADIUS = 100, 50
AURA_OPACITY_STOPS = [0, 0.6, 0.45, 0.32, 1, 0]  # (vị trí, độ đậm) từ tâm ra ngoài
AURA_OPACITY = (45, 100)  # % lúc nhỏ nhất → lớn nhất
AURA_SCALE = ([64, 92, 100], [74, 104, 100])  # % (ngang, dọc) lúc nhỏ nhất → lớn nhất

CENTER = 54


def static(v):
    return {"a": 0, "k": v}


def animated(frames_values, ease=True):
    kfs = []
    for i, (t, v) in enumerate(frames_values):
        kf = {"t": t, "s": v if isinstance(v, list) else [v]}
        if i < len(frames_values) - 1:
            kf["o"] = {"x": [0.42 if ease else 0], "y": [0 if ease else 0]}
            kf["i"] = {"x": [0.58 if ease else 1], "y": [1]}
        kfs.append(kf)
    return {"a": 1, "k": kfs}


def group(name, items):
    return {
        "ty": "gr",
        "nm": name,
        "it": items + [{
            "ty": "tr",
            "p": static([0, 0]), "a": static([0, 0]), "s": static([100, 100]),
            "r": static(0), "o": static(100), "sk": static(0), "sa": static(0),
        }],
    }


def rect(cx, cy, w, h, r):
    return {"ty": "rc", "d": 1, "p": static([cx, cy]), "s": static([w, h]), "r": static(r)}


def ellipse(cx, cy, d):
    return {"ty": "el", "d": 1, "p": static([cx, cy]), "s": static([d, d])}


def fill(color, opacity=100):
    return {"ty": "fl", "c": static(hex_to_rgb01(color) + [1]), "o": static(opacity), "r": 1}


def stroke(color, width, opacity):
    return {"ty": "st", "c": static(hex_to_rgb01(color) + [1]), "o": static(opacity), "w": static(width), "lc": 2, "lj": 2}


def edge_gradient(width, opacity, lighten=0.0, cap=2):
    stops = []
    for i, color in enumerate(EDGE_COLORS):
        offset = i / (len(EDGE_COLORS) - 1)
        stops += [int(offset) if offset in (0, 1) else offset] + hex_to_rgb01(color, lighten)
    return {
        "ty": "gs", "o": static(opacity), "w": static(width), "lc": cap, "lj": 2, "ml": 4, "t": 1,
        "g": {"p": len(EDGE_COLORS), "k": static(stops)},
        "s": static([PHONE_X, PHONE_Y]), "e": static([PHONE_X + PHONE_W, PHONE_Y + PHONE_H]),
    }


def phone():
    cx, cy = phone_center()
    return rect(cx, cy, PHONE_W, PHONE_H, PHONE_RADIUS)


def trim(start, end):
    return {"ty": "tm", "s": static(start), "e": static(end), "m": 1,
            "o": animated([(0, 0), (FRAMES, 360)], ease=False)}


def layer(index, name, shapes, opacity=None, scale=None):
    return {
        "ddd": 0, "ind": index, "ty": 4, "nm": name, "sr": 1, "ao": 0, "bm": 0,
        "ip": 0, "op": FRAMES, "st": 0,
        "ks": {
            "o": opacity or static(100),
            "r": static(0),
            "p": static([CENTER, CENTER, 0]),
            "a": static([CENTER, CENTER, 0]),
            "s": scale or static([100, 100, 100]),
        },
        "shapes": shapes,
    }


digit_shapes = []
for char, x0, y0 in digit_origins():
    for x, y, w, h in segment_boxes(x0, y0, char):
        digit_shapes.append(rect(round(x + w / 2, 3), round(y + h / 2, 3), round(w, 3), round(h, 3), min(w, h) / 2))

screen = layer(1, "screen", [
    group("digits", digit_shapes + [stroke(CLOCK_COLOR, CLOCK_GLOW_WIDTH, round(CLOCK_GLOW_ALPHA / 255 * 100)), fill(CLOCK_COLOR)]),
    group("camera", [ellipse(CAMERA_X, CAMERA_Y, 2 * CAMERA_RADIUS), fill(CAMERA_COLOR)]),
    group("dots", [ellipse(cx, DOTS_Y, 2 * DOT_RADIUS) for cx in DOTS_X] + [fill(DOT_COLOR)]),
])

comet_shapes = []
for width, steps, opacity, lighten, tail, front in LED_LAYERS:
    for k in range(1, steps + 1):
        start = PEAK - tail * (k / steps) ** TAIL_CURVE
        end = PEAK + front * k / steps
        comet_shapes.append(group(
            f"led {width} {k}",
            [phone(), trim(round(start, 2), round(end, 2)), edge_gradient(width, opacity, lighten, cap=1)],
        ))
comet = layer(2, "edge light", comet_shapes)

border = layer(3, "border", [
    group(name, [phone(), edge_gradient(width, round(alpha * 100))])
    for name, (width, alpha) in zip(("core", "glow", "halo"), BORDER_STROKES)
])
body = layer(4, "body", [group("screen off", [phone(), fill(SCREEN_COLOR)])])

aura_stops = []
for i, color in enumerate(EDGE_COLORS):
    offset = i / (len(EDGE_COLORS) - 1)
    aura_stops += [int(offset) if offset in (0, 1) else offset] + hex_to_rgb01(color)
aura_gradient = {
    "ty": "gf", "o": static(100), "r": 1, "t": 2,
    "g": {"p": len(EDGE_COLORS), "k": static(aura_stops + AURA_OPACITY_STOPS)},
    "s": static([CENTER, CENTER]), "e": static([CENTER + AURA_RADIUS, CENTER]), "h": static(0), "a": static(0),
}
aura = layer(
    5, "aura", [group("glow", [ellipse(CENTER, CENTER, AURA_DIAMETER), aura_gradient])],
    opacity=animated([(0, AURA_OPACITY[0]), (FRAMES / 2, AURA_OPACITY[1]), (FRAMES, AURA_OPACITY[0])]),
    scale=animated([(0, AURA_SCALE[0]), (FRAMES / 2, AURA_SCALE[1]), (FRAMES, AURA_SCALE[0])]),
)

lottie = {
    "v": "5.12.2", "nm": "device edge light", "ddd": 0,
    "fr": FPS, "ip": 0, "op": FRAMES, "w": 108, "h": 108,
    "assets": [], "markers": [],
    "layers": [screen, comet, border, body, aura],
}
OUT.parent.mkdir(parents=True, exist_ok=True)
data = json.dumps(lottie, separators=(",", ":"))
io.open(OUT, "w", encoding="utf-8", newline="\n").write(data)
print("wrote", OUT.relative_to(RES.parents[3]), len(data), "bytes")
