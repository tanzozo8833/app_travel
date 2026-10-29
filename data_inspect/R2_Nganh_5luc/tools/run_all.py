"""Chạy toàn bộ quy trình R2: tải dữ liệu (dùng cache nếu có) → phân tích → tổng hợp → dashboard → bảng kê nguồn.

    cd data_inspect/R2_Nganh_5luc/tools
    python run_all.py            # dùng dữ liệu đã tải
    python run_all.py --fetch    # tải các nguồn còn thiếu trước

Yêu cầu: pandas numpy scipy statsmodels matplotlib plotly scikit-learn requests pytrends google-play-scraper
"""
import subprocess
import sys

STEPS = ["analyze_overview.py", "analyze_competition.py", "analyze_buyers.py", "analyze_suppliers.py",
         "analyze_synthesis.py", "build_dashboard.py", "sources.py"]

if __name__ == "__main__":
    if "--fetch" in sys.argv:
        subprocess.run([sys.executable, "fetch_data.py"], check=False)
    for s in STEPS:
        print("==>", s, flush=True)
        subprocess.run([sys.executable, s], check=True)
