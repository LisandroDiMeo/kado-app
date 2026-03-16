from PIL import Image, ImageDraw, ImageFont

# Config
BG_COLOR = (250, 148, 66)  # #FA9442
TEXT_COLOR = (0, 0, 0)
CANVAS_W, CANVAS_H = 512, 900
CORNER_RADIUS = 60
FONT_SIZE = 200
FONT_PATH = "composeApp/src/commonMain/composeResources/font/mplus1code_Bold.ttf"

# Create image with transparency
img = Image.new("RGBA", (CANVAS_W, CANVAS_H), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Draw rounded rectangle background
draw.rounded_rectangle(
    [(0, 0), (CANVAS_W, CANVAS_H)],
    radius=CORNER_RADIUS,
    fill=BG_COLOR,
)

# Load font
font = ImageFont.truetype(FONT_PATH, FONT_SIZE)

# Characters to draw vertically
chars = ["カ", "ー", "ド"]

# Measure each character and compute total height
bboxes = [font.getbbox(ch) for ch in chars]
char_heights = [bb[3] - bb[1] for bb in bboxes]
char_widths = [bb[2] - bb[0] for bb in bboxes]

# Per-gap spacing: [after カ, after ー]
gaps = [60, 20]
total_text_h = sum(char_heights) + sum(gaps)

# Center vertically
y = (CANVAS_H - total_text_h) // 2

for i, ch in enumerate(chars):
    bb = bboxes[i]
    cw = bb[2] - bb[0]
    # Center horizontally
    x = (CANVAS_W - cw) // 2 - bb[0]
    draw.text((x, y - bb[1]), ch, font=font, fill=TEXT_COLOR)
    y += char_heights[i] + (gaps[i] if i < len(gaps) else 0)

# Save full-res logo
img.save("kado_logo.png", "PNG")
print("Saved kado_logo.png")

# Also generate a square 1024x1024 app icon version (with padding)
ICON_SIZE = 1024
icon = Image.new("RGBA", (ICON_SIZE, ICON_SIZE), (0, 0, 0, 0))
# Scale the logo to fit within the icon with padding
pad = 80
max_h = ICON_SIZE - 2 * pad
max_w = ICON_SIZE - 2 * pad
scale = min(max_w / CANVAS_W, max_h / CANVAS_H)
new_w = int(CANVAS_W * scale)
new_h = int(CANVAS_H * scale)
resized = img.resize((new_w, new_h), Image.LANCZOS)
offset_x = (ICON_SIZE - new_w) // 2
offset_y = (ICON_SIZE - new_h) // 2
icon.paste(resized, (offset_x, offset_y), resized)
icon.save("kado_icon_1024.png", "PNG")
print("Saved kado_icon_1024.png (1024x1024 app icon)")
