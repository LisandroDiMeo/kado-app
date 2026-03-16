from PIL import Image, ImageDraw, ImageFont
import os
import shutil

FONT_PATH = "composeApp/src/commonMain/composeResources/font/mplus1code_Bold.ttf"
BG_COLOR = (250, 148, 66)  # #FA9442
TEXT_COLOR = (0, 0, 0)

def generate_base_logo(size=1024):
    """Generate a square logo at the given size."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Full square background (no rounded corners — OS applies its own mask)
    draw.rectangle([(0, 0), (size, size)], fill=BG_COLOR)

    # Scale font relative to canvas
    font_size = int(size * 0.22)
    font = ImageFont.truetype(FONT_PATH, font_size)

    chars = ["カ", "ー", "ド"]
    bboxes = [font.getbbox(ch) for ch in chars]
    char_heights = [bb[3] - bb[1] for bb in bboxes]

    # Per-gap spacing: [after カ, after ー]
    gap_ka = int(size * 0.066)
    gap_dash = int(size * 0.022)
    gaps = [gap_ka, gap_dash]

    total_text_h = sum(char_heights) + sum(gaps)
    y = (size - total_text_h) // 2

    for i, ch in enumerate(chars):
        bb = bboxes[i]
        cw = bb[2] - bb[0]
        x = (size - cw) // 2 - bb[0]
        draw.text((x, y - bb[1]), ch, font=font, fill=TEXT_COLOR)
        y += char_heights[i] + (gaps[i] if i < len(gaps) else 0)

    return img


def generate_adaptive_foreground(size=1024):
    """Generate adaptive icon foreground (108dp safe zone, content in inner 66dp).
    The foreground layer is 108x108dp but only the inner 72dp circle is guaranteed visible.
    We render content centered in the full canvas with padding."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Content should be in roughly the inner 66% of the canvas
    padding = int(size * 0.17)
    content_size = size - 2 * padding

    font_size = int(content_size * 0.22)
    font = ImageFont.truetype(FONT_PATH, font_size)

    chars = ["カ", "ー", "ド"]
    bboxes = [font.getbbox(ch) for ch in chars]
    char_heights = [bb[3] - bb[1] for bb in bboxes]

    gap_ka = int(content_size * 0.066)
    gap_dash = int(content_size * 0.022)
    gaps = [gap_ka, gap_dash]

    total_text_h = sum(char_heights) + sum(gaps)
    y = (size - total_text_h) // 2

    for i, ch in enumerate(chars):
        bb = bboxes[i]
        cw = bb[2] - bb[0]
        x = (size - cw) // 2 - bb[0]
        draw.text((x, y - bb[1]), ch, font=font, fill=TEXT_COLOR)
        y += char_heights[i] + (gaps[i] if i < len(gaps) else 0)

    return img


# --- iOS ---
print("Generating iOS icon...")
ios_icon = generate_base_logo(1024)
ios_dest = "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"
ios_icon.save(ios_dest, "PNG")
print(f"  Saved {ios_dest}")

# --- Android legacy mipmap icons ---
ANDROID_RES = "androidApp/src/main/res"
MIPMAP_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

print("Generating Android mipmap icons...")
base_1024 = generate_base_logo(1024)
for folder, size in MIPMAP_SIZES.items():
    resized = base_1024.resize((size, size), Image.LANCZOS).convert("RGBA")
    path = os.path.join(ANDROID_RES, folder)

    # Square icon
    sq_path = os.path.join(path, "ic_launcher.png")
    resized.save(sq_path, "PNG")
    print(f"  Saved {sq_path} ({size}x{size})")

    # Round icon (same image — Android applies circular mask)
    rnd_path = os.path.join(path, "ic_launcher_round.png")
    resized.save(rnd_path, "PNG")
    print(f"  Saved {rnd_path} ({size}x{size})")

# --- Android adaptive icon foreground ---
# Adaptive icons use 108dp layers; we generate at xxxhdpi (432px) and let Android scale
ADAPTIVE_FG_SIZES = {
    "drawable-mdpi": 108,
    "drawable-hdpi": 162,
    "drawable-xhdpi": 216,
    "drawable-xxhdpi": 324,
    "drawable-xxxhdpi": 432,
}

print("Generating Android adaptive icon layers...")
fg_base = generate_adaptive_foreground(1024)
bg_base = Image.new("RGBA", (1024, 1024), BG_COLOR)

for folder, size in ADAPTIVE_FG_SIZES.items():
    path = os.path.join(ANDROID_RES, folder)
    os.makedirs(path, exist_ok=True)

    fg_resized = fg_base.resize((size, size), Image.LANCZOS)
    fg_path = os.path.join(path, "ic_launcher_foreground.png")
    fg_resized.save(fg_path, "PNG")
    print(f"  Saved {fg_path} ({size}x{size})")

    bg_resized = bg_base.resize((size, size), Image.LANCZOS)
    bg_path = os.path.join(path, "ic_launcher_background.png")
    bg_resized.save(bg_path, "PNG")
    print(f"  Saved {bg_path} ({size}x{size})")

# Update adaptive icon XMLs to reference PNG drawables instead of vector XMLs
ic_launcher_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
"""

for xml_name in ["ic_launcher.xml", "ic_launcher_round.xml"]:
    xml_path = os.path.join(ANDROID_RES, "mipmap-anydpi-v26", xml_name)
    with open(xml_path, "w") as f:
        f.write(ic_launcher_xml)
    print(f"  Updated {xml_path}")

# Remove old vector drawables that are now replaced by PNGs
old_vectors = [
    os.path.join(ANDROID_RES, "drawable", "ic_launcher_background.xml"),
    os.path.join(ANDROID_RES, "drawable-v24", "ic_launcher_foreground.xml"),
]
for v in old_vectors:
    if os.path.exists(v):
        os.remove(v)
        print(f"  Removed old vector {v}")

print("\nDone! All app icons updated.")
