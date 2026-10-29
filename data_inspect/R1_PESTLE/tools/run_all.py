"""Chạy toàn bộ quy trình: tải dữ liệu (dùng cache nếu có) → phân tích → dựng dashboard.

    cd data_inspect/tools
    python run_all.py            # dùng dữ liệu đã tải
    python run_all.py --fetch    # tải lại các nguồn còn thiếu trước

Yêu cầu: pandas numpy scipy statsmodels matplotlib plotly requests pytrends
"""
import subprocess
import sys

STEPS = ["analyze_macro.py", "analyze_market.py", "analyze_trends.py", "analyze_space.py", "build_dashboard.py"]

if __name__ == "__main__":
    if "--fetch" in sys.argv:
        subprocess.run([sys.executable, "fetch_data.py"], check=False)
    for s in STEPS:
        print("==>", s)
        subprocess.run([sys.executable, s], check=True)
