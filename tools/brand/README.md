# tools/brand — icon app và animation của icon

Hai file sinh từ cùng một bộ hình học (`brand_geometry.py`), nên khung đầu của animation trùng icon
tĩnh và splash không nhảy khi chuyển từ splash hệ thống (Android 12+) sang splash của app.

| Script                          | Sinh ra                                                                               |
|---------------------------------|---------------------------------------------------------------------------------------|
| `generate_launcher_icon.py`     | `app/src/main/res/drawable/ic_launcher_foreground.xml` · `ic_launcher_monochrome.xml` |
| `generate_edge_light_lottie.py` | `app/src/main/res/raw/lottie_device_edge_light.json`                                  |

Chạy từ gốc repo (Python 3, chỉ thư viện chuẩn):

```bash
python tools/brand/generate_launcher_icon.py
python tools/brand/generate_edge_light_lottie.py
```

## Sửa gì ở đâu

- Màu viền sáng (`EDGE_COLORS`), màu đồng hồ, hình điện thoại, giờ hiện trên icon (`CLOCK_ROWS`),
  camera, chấm thông báo, các lớp viền tĩnh (`BORDER_STROKES`): `brand_geometry.py`, rồi chạy lại *
  *cả hai** script.
- Tốc độ (`FRAMES`), vị trí đỉnh sáng (`PEAK`), độ dài đuôi / mép trước, độ sáng và độ rộng từng
  lớp (`LED_LAYERS`, `TAIL_CURVE`), aura (`AURA_*`): đầu `generate_edge_light_lottie.py`.
- Đừng sửa tay file sinh ra: lần chạy sau sẽ ghi đè.

## Lưu ý

- Mọi hình nằm trong vùng an toàn 66dp giữa khung 108 (bán kính 33 quanh tâm 54, 54). Mỗi `pathData`
  của vector dưới 800 ký tự (lint `VectorPath`) — script tự kiểm.
- Không dùng hiệu ứng Gaussian Blur của Lottie: lottie-android vẽ nó bằng `BlurMaskFilter` theo px
  màn hình, không co giãn theo cỡ animation, nên mỗi cỡ mờ một kiểu. Độ mờ / quầng sáng làm bằng
  nhiều lớp nét rộng dần, nhạt dần.
- Small icon của thông báo (`ic_aod`) không sinh ở đây: nó phải là hình phẳng một màu cỡ 24dp.
- Đổi xong: build lại và xem trên máy — splash, icon launcher, icon theo chủ đề (Android 13+).
