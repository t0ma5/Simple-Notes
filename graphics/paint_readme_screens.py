from PIL import Image, ImageDraw, ImageFont

font_dir = r"D:\WEBSITES\Simple-Notes\graphics\fonts"
out_dir = r"D:\WEBSITES\Simple-Notes\graphics\screenshots"
W, H = 720, 1280
DP = 2
RED = (211, 47, 47, 255)
BG = (250, 250, 250, 255)
CARD = (255, 255, 255, 255)
INK = (33, 33, 33, 255)
MUTED = (97, 97, 97, 255)
LINE = (224, 224, 224, 255)
WHITE = (255, 255, 255, 255)
PAPER = (255, 248, 231, 255)
TB = 56 * DP


def d(n):
    return int(round(n * DP))


def font(name, size_sp):
    return ImageFont.truetype(f"{font_dir}\\Montserrat-{name}.ttf", int(round(size_sp * DP)))


def wrap(draw, text, fnt, max_w, max_lines):
    words = text.split()
    lines, cur = [], ""
    leftover = False
    for word in words:
        trial = word if not cur else f"{cur} {word}"
        if draw.textbbox((0, 0), trial, font=fnt)[2] <= max_w:
            cur = trial
        else:
            if cur:
                lines.append(cur)
            cur = word
            if len(lines) == max_lines:
                leftover = True
                break
    if cur and len(lines) < max_lines:
        lines.append(cur)
        cur = ""
    if len(lines) == max_lines and (leftover or cur):
        last = lines[-1]
        while draw.textbbox((0, 0), last + "…", font=fnt)[2] > max_w and last:
            last = last[:-1]
        lines[-1] = last.rstrip() + "…"
    return lines[:max_lines]


def icon_search(draw, cx, cy, s=None):
    s = s or d(24)
    r = s * 0.32
    w = max(2, d(2))
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=WHITE, width=w)
    draw.line((cx + r * 0.72, cy + r * 0.72, cx + s * 0.38, cy + s * 0.38), fill=WHITE, width=w)


def icon_book(draw, x, y, s=None):
    s = s or d(24)
    w = s * 0.78
    draw.rounded_rectangle((x, y, x + w, y + s), radius=d(2), fill=WHITE)
    half = x + w * 0.42
    draw.polygon(
        [
            (x, y),
            (half, y),
            (half, y + s * 0.52),
            ((x + half) / 2, y + s * 0.38),
            (x, y + s * 0.52),
        ],
        fill=RED,
    )


def icon_page(draw, x, y, s=None):
    s = s or d(24)
    draw.rounded_rectangle((x, y, x + s * 0.72, y + s), radius=d(2), fill=WHITE)
    draw.polygon([(x + s * 0.38, y), (x + s * 0.72, y + s * 0.28), (x + s * 0.38, y + s * 0.28)], fill=RED)
    for i in range(2):
        yy = y + s * 0.48 + i * d(5)
        draw.line((x + d(4), yy, x + s * 0.58, yy), fill=RED, width=d(1.5))


def icon_back(draw, x, y, s=None):
    s = s or d(24)
    draw.polygon([(x + s * 0.55, y), (x, y + s / 2), (x + s * 0.55, y + s)], fill=WHITE)
    draw.rectangle((x + s * 0.28, y + s * 0.38, x + s, y + s * 0.62), fill=WHITE)


def icon_overflow(draw, cx, cy):
    r = d(2)
    for i in (-8, 0, 8):
        draw.ellipse((cx - r, cy + i - r, cx + r, cy + i + r), fill=WHITE)


def icon_drag(draw, x, y, color=MUTED):
    gap = d(4)
    dot = d(2)
    for row in range(2):
        for col in range(3):
            ox = x + col * gap
            oy = y + row * gap
            draw.ellipse((ox, oy, ox + dot, oy + dot), fill=color)


def notebook_paper(draw, cx, cy, s=None):
    s = s or d(48)
    w, h = s * 0.72, s
    x, y = cx - w / 2, cy - h / 2
    draw.rounded_rectangle((x, y, x + w, y + h), radius=d(3), fill=PAPER, outline=LINE, width=d(1))
    bx = x + d(2)
    half = x + w * 0.48
    draw.polygon(
        [
            (bx, y + d(5)),
            (half, y + d(5)),
            (half, y + h * 0.46),
            ((bx + half) / 2, y + h * 0.36),
            (bx, y + h * 0.46),
        ],
        fill=RED,
    )
    for i in range(3):
        yy = y + h * 0.56 + i * (s * 0.11)
        draw.line((x + d(6), yy, x + w - d(6), yy), fill=MUTED, width=d(1))


def toolbar(draw, title, mode):
    draw.rectangle((0, 0, W, TB), fill=RED)
    hit = d(48)
    icon = d(24)
    title_font = font("Medium", 21)
    actions = ("toggle", "search") if mode == "inside" else ("overflow", "toggle", "search")
    x = d(16)
    if mode == "inside":
        icon_back(draw, d(16), (TB - icon) // 2)
        x = hit
    icons_left = W - len(actions) * hit
    shown = title
    while draw.textbbox((0, 0), shown, font=title_font)[2] > icons_left - x - d(8) and len(shown) > 1:
        shown = shown[:-1].rstrip() + "…"
    bbox = draw.textbbox((0, 0), shown, font=title_font)
    th = bbox[3] - bbox[1]
    draw.text((x, (TB - th) // 2 - bbox[1]), shown, font=title_font, fill=WHITE)
    right = W
    for kind in actions:
        right -= hit
        cx = right + hit // 2
        cy = TB // 2
        if kind == "search":
            icon_search(draw, cx, cy)
        elif kind == "overflow":
            icon_overflow(draw, cx, cy)
        elif mode == "notes":
            icon_book(draw, cx - icon * 0.36, cy - icon / 2)
        else:
            icon_page(draw, cx - icon * 0.36, cy - icon / 2)


def fab(draw):
    r = d(28)
    cx, cy = W - d(16) - r, H - d(16) - r
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=RED)
    arm = d(12)
    thick = d(2)
    draw.rectangle((cx - thick // 2, cy - arm, cx + thick // 2 + 1, cy + arm), fill=WHITE)
    draw.rectangle((cx - arm, cy - thick // 2, cx + arm, cy + thick // 2 + 1), fill=WHITE)


def note_card(draw, box, title, body, notebook=None):
    x0, y0, x1, y1 = box
    draw.rounded_rectangle(box, radius=d(12), fill=CARD, outline=LINE, width=d(1))
    pad = d(12)
    title_f = font("SemiBold", 16)
    body_f = font("Regular", 14)
    where_f = font("Medium", 12)
    draw.text((x0 + pad, y0 + d(10)), title, font=title_f, fill=INK)
    max_w = x1 - x0 - pad * 2
    lines = wrap(draw, body, body_f, max_w, 3)
    y = y0 + d(34)
    for line in lines:
        draw.text((x0 + pad, y), line, font=body_f, fill=INK)
        y += d(18)
    if notebook:
        tw = draw.textbbox((0, 0), notebook, font=where_f)[2]
        draw.text((x1 - pad - tw, y1 - d(22)), notebook, font=where_f, fill=MUTED)


def notebook_tile(draw, box, title, count):
    x0, y0, x1, y1 = box
    draw.rounded_rectangle(box, radius=d(12), fill=CARD, outline=LINE, width=d(1))
    draw.text((x0 + d(12), y0 + d(12)), title, font=font("SemiBold", 18), fill=INK)
    notebook_paper(draw, (x0 + x1) / 2, (y0 + y1) / 2 + d(4), d(52))
    draw.text((x0 + d(12), y1 - d(28)), count, font=font("Medium", 12), fill=MUTED)
    icon_drag(draw, x1 - d(28), y1 - d(24))


def paint_notes():
    im = Image.new("RGBA", (W, H), BG)
    draw = ImageDraw.Draw(im)
    toolbar(draw, "Notes", "notes")
    cards = [
        ("Shopping", "Milk, eggs, bread, coffee. Pick up basil if they have it, and check the dates.", "Notes"),
        ("Standup", "Ship the notes list, then widget colors on every home screen before Friday.", "Work"),
        ("Flight", "ATH 14:10, seat 12A. Gate closes 13:40. Taxi from the airport after landing.", "Travel"),
        ("Pasta", "Garlic, tomato, basil. Boil twelve minutes, save pasta water for the sauce.", "Recipes"),
        ("Gym", "Legs today, then stretch. Pack the blue bottle and a towel for the locker.", "Notes"),
        ("Hiring", "Ask Tom for the Android JD and send it before Friday with the salary band.", "Work"),
    ]
    top = TB + d(8)
    gap = d(8)
    col_w = (W - d(16) * 2 - gap) // 2
    card_h = d(118)
    for i, (title, body, nb) in enumerate(cards):
        c, r = i % 2, i // 2
        x0 = d(16) + c * (col_w + gap)
        y0 = top + r * (card_h + gap)
        note_card(draw, (x0, y0, x0 + col_w, y0 + card_h), title, body, nb)
    fab(draw)
    im.convert("RGB").save(f"{out_dir}\\screenshot-notes.png", quality=95)


def paint_notebooks():
    im = Image.new("RGBA", (W, H), BG)
    draw = ImageDraw.Draw(im)
    toolbar(draw, "Notebooks", "notebooks")
    tiles = [
        ("Notes", "4 notes"),
        ("Work", "3 notes"),
        ("Travel", "2 notes"),
        ("Recipes", "1 note"),
    ]
    top = TB + d(8)
    gap = d(8)
    col_w = (W - d(16) * 2 - gap) // 2
    tile_h = col_w
    for i, (name, count) in enumerate(tiles):
        c, r = i % 2, i // 2
        x0 = d(16) + c * (col_w + gap)
        y0 = top + r * (tile_h + gap)
        notebook_tile(draw, (x0, y0, x0 + col_w, y0 + tile_h), name, count)
    fab(draw)
    im.convert("RGB").save(f"{out_dir}\\screenshot-notebooks.png", quality=95)


def paint_inside():
    im = Image.new("RGBA", (W, H), BG)
    draw = ImageDraw.Draw(im)
    toolbar(draw, "Work - Notebook", "inside")
    cards = [
        ("Standup", "Ship the notes list, then widget colors on every home screen before Friday."),
        ("Hiring", "Ask Tom for the Android JD and send it before Friday with the salary band."),
        ("Q3", "No internet, backups, FOSS. Keep the fork next to Pro and ship the list."),
        ("Bugs", "Toolbar under the status bar is fixed. Recheck recycle bin padding after."),
        ("Agenda", "Call Maria at 16:00. Share the APK after the list view lands on Github."),
        ("Done", "Read-only mode ships. Encrypted export stays in settings where it belongs."),
    ]
    top = TB + d(8)
    gap = d(8)
    col_w = (W - d(16) * 2 - gap) // 2
    card_h = d(102)
    for i, (title, body) in enumerate(cards):
        c, r = i % 2, i // 2
        x0 = d(16) + c * (col_w + gap)
        y0 = top + r * (card_h + gap)
        note_card(draw, (x0, y0, x0 + col_w, y0 + card_h), title, body)
    fab(draw)
    im.convert("RGB").save(f"{out_dir}\\screenshot-inside-notebook.png", quality=95)


if __name__ == "__main__":
    paint_notes()
    paint_notebooks()
    paint_inside()
    print("wrote notes, notebooks, inside-notebook")
