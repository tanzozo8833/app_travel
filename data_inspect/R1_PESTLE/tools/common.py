"""Đường dẫn, tải có bộ nhớ đệm, kiểu biểu đồ dùng chung cho mọi công cụ trong data_inspect."""
import json
import os
import time
from pathlib import Path

import requests

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "data" / "raw"
PROC = ROOT / "data" / "processed"
RES = ROOT / "results"
IMG = RES / "img"
for p in (RAW, PROC, RES, IMG):
    p.mkdir(parents=True, exist_ok=True)

UA = {"User-Agent": "LevartResearch/0.1 (student market research; tan963682@gmail.com)"}


def get(url, params=None, cache=None, as_json=True, timeout=90, retries=4, sleep=2.0):
    """GET có cache trên đĩa: lần chạy sau đọc lại tệp raw, không gọi mạng."""
    if cache:
        fp = RAW / cache
        if fp.exists():
            txt = fp.read_text(encoding="utf-8")
            return json.loads(txt) if as_json else txt
    last = None
    for i in range(retries):
        try:
            r = requests.get(url, params=params, headers=UA, timeout=timeout)
            if r.status_code == 200:
                if cache:
                    (RAW / cache).write_text(r.text, encoding="utf-8")
                    _log_request(r.url, cache)
                return r.json() if as_json else r.text
            last = f"HTTP {r.status_code}"
        except requests.RequestException as e:
            last = repr(e)
        time.sleep(sleep * (2 ** i))
    raise RuntimeError(f"Tải thất bại {url}: {last}")


def _log_request(url, cache):
    """Nhật ký mọi lần tải thật (không tính đọc cache): URL đầy đủ sau khi ghép tham số."""
    import csv
    from datetime import datetime
    fp = RAW / "_requests_log.csv"
    new = not fp.exists()
    with open(fp, "a", encoding="utf-8-sig", newline="") as f:
        w = csv.writer(f)
        if new:
            w.writerow(["time", "file", "url"])
        w.writerow([datetime.now().strftime("%Y-%m-%d %H:%M:%S"), f"data/raw/{cache}", url])


def save_json(obj, name):
    (RES / name).write_text(json.dumps(obj, ensure_ascii=False, indent=2, default=float), encoding="utf-8")


def load_summary():
    fp = RES / "summary.json"
    return json.loads(fp.read_text(encoding="utf-8")) if fp.exists() else {}


def update_summary(section, data):
    s = load_summary()
    s[section] = data
    save_json(s, "summary.json")


# ---- Biểu đồ: một bảng màu, nền sáng, chữ đủ tương phản ----
SERIES = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300", "#6250d6", "#e34948"]
INK, INK2, MUTED, GRID = "#0b0b0b", "#52514e", "#898781", "#e1e0d9"


def style():
    import matplotlib as mpl
    mpl.rcParams.update({
        "figure.dpi": 110, "savefig.dpi": 150, "savefig.bbox": "tight",
        "font.family": ["Segoe UI", "DejaVu Sans"], "font.size": 10,
        "axes.edgecolor": "#c3c2b7", "axes.labelcolor": INK2, "axes.titlesize": 12,
        "axes.titleweight": "bold", "axes.titlelocation": "left", "axes.grid": True,
        "grid.color": GRID, "grid.linewidth": 0.6, "axes.spines.top": False, "axes.spines.right": False,
        "xtick.color": INK2, "ytick.color": INK2, "legend.frameon": False,
        "axes.formatter.useoffset": False, "axes.prop_cycle": mpl.cycler(color=SERIES), "figure.facecolor": "white",
    })


def savefig(fig, name, note=None):
    """Lưu biểu đồ; luôn chèn dòng nguồn (theo sources.FIG_SOURCES) phía trên ghi chú phương pháp."""
    import textwrap
    from sources import FIG_SOURCES, cite
    lines = []
    if name in FIG_SOURCES:
        lines += textwrap.wrap(cite(*FIG_SOURCES[name]), 190)
    if note:
        lines += textwrap.wrap("Phương pháp: " + note.replace("Nguồn: ", "Dữ liệu: "), 190)
    if lines:
        fig.text(0.01, -0.02, "\n".join(lines), fontsize=7.5, color=MUTED, ha="left", va="top", linespacing=1.35)
    fig.savefig(IMG / name)
    import matplotlib.pyplot as plt
    plt.close(fig)
    return f"results/img/{name}"
