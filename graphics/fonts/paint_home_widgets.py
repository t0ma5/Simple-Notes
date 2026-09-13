import random
from PIL import Image, ImageDraw, ImageFont

font_dir = r"D:\WEBSITES\Simple-Notes\graphics\fonts"
roboto_dir = r"D:\WEBSITES\thesis\fonts\Roboto"
out_dir = r"D:\WEBSITES\Simple-Notes\graphics\screenshots"
W, H = 720, 1280
DP = 2
APP_RED = (211, 47, 47, 255)
NEAR_BLACK = (28, 28, 32, 255)
WHITE = (255, 255, 255, 255)
DONE_A = 102
RADIUS = 12
TITLE_SP = 12
BODY_SP = 17
TITLE_PAD = 2
TEXT_PAD = 4
CHECK_PAD_S = 16
CHECK_PAD_V = 8
MIN_WIDGET = 110


def d(n):
    return int(round(n * DP))


def roboto(weight, size_sp):
    size = int(round(size_sp * DP))
    name = "Roboto-Medium.ttf" if weight == "medium" else "Roboto-Regular.ttf"
    try:
        return ImageFont.truetype(f"{roboto_dir}\\{name}", size)
    except OSError:
        fallback = "Montserrat-Medium.ttf" if weight == "medium" else "Montserrat-Regular.ttf"
        return ImageFont.truetype(f"{font_dir}\\{fallback}", size)


def line_h(fnt):
    ascent, descent = fnt.getmetrics()
    return ascent + descent


def wrap(draw, text, fnt, max_width):
    words = text.split()
    lines, cur = [], ""
    for word in words:
        trial = word if not cur else f"{cur} {word}"
        if draw.textbbox((0, 0), trial, font=fnt)[2] <= max_width:
            cur = trial
        else:
            if cur:
                lines.append(cur)
            cur = word
    if cur:
        lines.append(cur)
    return lines


def space_wallpaper():
    rng = random.Random(13)
    im = Image.new("RGB", (W, H), (5, 7, 14))
    px = im.load()
    for x in range(W):
        streak = 4 + (x * 3) % 9
        for y in range(H):
            n = rng.randint(0, 18)
            px[x, y] = (streak + n // 4, 6 + n // 3, 14 + n // 2)
    draw = ImageDraw.Draw(im)
    for _ in range(160):
        x = rng.randint(0, W - 1)
        y = rng.randint(0, H - 1)
        b = rng.randint(150, 255)
        r = rng.choice([0, 0, 1, 1, 2])
        draw.ellipse((x, y, x + r, y + r), fill=(b, b, min(255, b + 25)))
    return im


def paint_home():
    src = f"{out_dir}\\screenshot-home-widgets.png"
    im = space_wallpaper().convert("RGBA")
    draw = ImageDraw.Draw(im)

    title_font = roboto("regular", TITLE_SP)
    body_font = roboto("regular", BODY_SP)
    label_font = roboto("regular", 12)
    time_font = roboto("medium", 14)
    title_lh = line_h(title_font)
    body_lh = line_h(body_font)
    radius = d(RADIUS)
    white = WHITE

    sb = d(24)
    draw.text((d(16), d(4)), "10:00", font=time_font, fill=white)
    bx = W - d(18)
    draw.rounded_rectangle((bx, d(8), bx + d(16), d(16)), radius=d(2), outline=white, width=d(1))
    draw.rectangle((bx + d(16), d(10), bx + d(18), d(14)), fill=white)
    draw.rectangle((bx + d(2), d(10), bx + d(13), d(14)), fill=white)
    draw.polygon([(bx - d(22), d(16)), (bx - d(12), d(8)), (bx - d(12), d(16))], fill=white)
    draw.line((bx - d(38), d(8), bx - d(38), d(16)), fill=white, width=d(1))
    draw.line((bx - d(42), d(10), bx - d(38), d(10)), fill=white, width=d(1))
    draw.line((bx - d(42), d(13), bx - d(38), d(13)), fill=white, width=d(1))
    draw.line((bx - d(42), d(16), bx - d(38), d(16)), fill=white, width=d(1))

    gap = d(20)
    side = d(12)
    col_w = (W - side * 2 - gap) // 2
    cell_h = max(d(MIN_WIDGET), col_w)
    empty_row = cell_h // 2
    top = sb + empty_row

    def title_row_h(show_title):
        if not show_title:
            return 0
        return d(TITLE_PAD) * 2 + title_lh

    def draw_title(box, title):
        x0, y0, x1, _ = box
        tw = draw.textbbox((0, 0), title, font=title_font)[2]
        draw.text(((x0 + x1 - tw) / 2, y0 + d(TITLE_PAD)), title, font=title_font, fill=white)

    def fill_card(box, fill):
        nonlocal im, draw
        if len(fill) == 4 and fill[3] < 255:
            overlay = Image.new("RGBA", im.size, (0, 0, 0, 0))
            ImageDraw.Draw(overlay).rounded_rectangle(box, radius=radius, fill=fill)
            im = Image.alpha_composite(im, overlay)
            draw = ImageDraw.Draw(im)
        else:
            draw.rounded_rectangle(box, radius=radius, fill=fill)

    blurb = "Simple Notes is a FOSS notes app for Android with no internet access, no ads, no tracking, and encrypted backups. Find it on Github!"
    ph = (side, top, W - side, top + cell_h)
    fill_card(ph, (18, 24, 34, 115))
    draw_title(ph, "Widget Transparent Background")
    max_w = ph[2] - ph[0] - d(TEXT_PAD) * 2
    y = ph[1] + title_row_h(True) + d(TEXT_PAD)
    for line in wrap(draw, blurb, body_font, max_w):
        if y + body_lh > ph[3] - d(TEXT_PAD):
            break
        draw.text((ph[0] + d(TEXT_PAD), y), line, font=body_font, fill=white)
        y += body_lh

    pair_top = top + cell_h + gap
    left = (side, pair_top, side + col_w, pair_top + cell_h)
    fill_card(left, APP_RED)
    draw_title(left, "Features")
    y = left[1] + title_row_h(True) + d(TEXT_PAD)
    for line in ["No Internet", "No Tracking", "No Ads", "Local Backups", "FOSS"]:
        draw.text((left[0] + d(TEXT_PAD), y), line, font=body_font, fill=white)
        y += body_lh

    right = (side + col_w + gap, pair_top, side + col_w + gap + col_w, pair_top + cell_h)
    fill_card(right, NEAR_BLACK)
    y = right[1]
    for text, done in [("Gym 6pm", False), ("Update App", True), ("Call Tom", False), ("VolunteersBase", False)]:
        fill = (255, 255, 255, DONE_A) if done else white
        ty = y + d(CHECK_PAD_V)
        draw.text((right[0] + d(CHECK_PAD_S), ty), text, font=body_font, fill=fill)
        if done:
            bbox = draw.textbbox((right[0] + d(CHECK_PAD_S), ty), text, font=body_font)
            mid = (bbox[1] + bbox[3]) // 2
            draw.line((bbox[0], mid, bbox[2], mid), fill=fill, width=d(1))
        y += d(CHECK_PAD_V) * 2 + body_lh

    def load_icon(path, size, punch_black=False):
        icon = Image.open(path).convert("RGBA")
        if punch_black:
            pix = icon.load()
            iw, ih = icon.size
            for yy in range(ih):
                for xx in range(iw):
                    r, g, b, a = pix[xx, yy]
                    if r < 28 and g < 28 and b < 28:
                        pix[xx, yy] = (0, 0, 0, 0)
        icon = icon.resize((size, size), Image.Resampling.LANCZOS)
        if not punch_black:
            mask = Image.new("L", (size, size), 0)
            ImageDraw.Draw(mask).rounded_rectangle((0, 0, size - 1, size - 1), radius=int(size * 0.22), fill=255)
            rounded = Image.new("RGBA", (size, size), (0, 0, 0, 0))
            rounded.paste(icon, (0, 0), mask)
            icon = rounded
        return icon

    icon_size = d(48)
    apps = [
        (r"D:\WEBSITES\Simple-Notes\graphics\icon.png", "Notes", True),
        (r"D:\WEBSITES\Simple-Gallery\graphics\icon.png", "Gallery", True),
        (r"D:\WEBSITES\VB_PHP8\oc-content\themes\repurpose\app\pwa-192.png", "VolunteersBase", False),
        (r"D:\WEBSITES\spl1t\public\android-chrome-192x192.png", "Spl1t", False),
    ]
    dock_y = H - d(24) - d(48) - d(20) - d(16)
    slot_w = W / len(apps)
    for i, (path, label, punch) in enumerate(apps):
        icon = load_icon(path, icon_size, punch)
        cx = int(slot_w * (i + 0.5))
        im.paste(icon, (cx - icon_size // 2, dock_y), icon)
        tw = draw.textbbox((0, 0), label, font=label_font)[2]
        draw.text((cx - tw / 2, dock_y + icon_size + d(4)), label, font=label_font, fill=white)

    dots_y = H - d(12)
    dots_x = [W // 2 - d(18), W // 2 - d(6), W // 2 + d(6), W // 2 + d(18)]
    for i, dx in enumerate(dots_x):
        r = d(2.5) if i == 1 else d(2)
        fill = (255, 255, 255, 230) if i == 1 else (255, 255, 255, 110)
        draw.ellipse((dx - r, dots_y - r, dx + r, dots_y + r), fill=fill)

    im.convert("RGB").save(src, "PNG")
    print("home")


paint_home()
