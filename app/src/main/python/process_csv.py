import os
import json

try:
    import pandas as pd
    import matplotlib
    matplotlib.use("Agg")  # Non-interactive backend
    import matplotlib.pyplot as plt
except Exception as e:
    # If heavy deps aren't available yet, provide a graceful fallback
    pd = None
    plt = None


def process_csv(csv_path: str, output_dir: str):
    """
    Dummy CSV processor for Android via Chaquopy.

    - Reads the CSV at csv_path using pandas if available.
    - Computes a simple summary.
    - Generates a simple plot saved to output_dir as plot.png (if matplotlib available).

    Returns a dict with keys:
      - summary: str
      - plot_path: str (absolute path to saved plot image)
    """

    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)
    plot_path = os.path.join(output_dir, "plot.png")

    if pd is None or plt is None:
        # Fallback without heavy dependencies
        with open(csv_path, "rb") as f:
            size_bytes = len(f.read())
        summary = (
            "Pandas/Matplotlib not available yet.\n"
            f"CSV path: {csv_path}\n"
            f"File size: {size_bytes} bytes\n"
            "Install pandas/matplotlib via Gradle (Chaquopy) to enable full analysis."
        )
        # Create a tiny placeholder image
        try:
            from PIL import Image, ImageDraw
            img = Image.new("RGB", (600, 300), color=(230, 230, 230))
            d = ImageDraw.Draw(img)
            d.text((10, 10), "Plot placeholder", fill=(0, 0, 0))
            img.save(plot_path)
        except Exception:
            # If Pillow isn't available either, leave plot_path empty
            plot_path = ""
        return {"summary": summary, "plot_path": plot_path}

    # Read CSV with pandas
    try:
        df = pd.read_csv(csv_path)
    except Exception as e:
        return {"summary": f"Failed to read CSV: {e}", "plot_path": ""}

    # Basic summary
    rows, cols = df.shape
    summary_lines = [
        f"Rows: {rows}",
        f"Columns: {cols}",
        "\nNumeric column means:",
    ]
    numeric = df.select_dtypes(include=["number"]).mean(numeric_only=True)
    for col, val in numeric.items():
        summary_lines.append(f"  - {col}: {val:.3f}")

    summary = "\n".join(summary_lines)

    # Simple plot: if there's at least one numeric column, plot the first 50 values
    try:
        if not numeric.empty:
            col_name = numeric.index[0]
            plt.figure(figsize=(6, 3))
            df[col_name].head(50).plot(title=f"First 50 of {col_name}")
            plt.tight_layout()
            plt.savefig(plot_path)
            plt.close()
        else:
            # Create an empty placeholder plot
            plt.figure(figsize=(6, 3))
            plt.text(0.5, 0.5, "No numeric columns", ha='center', va='center')
            plt.axis('off')
            plt.savefig(plot_path)
            plt.close()
    except Exception as e:
        # If plotting fails, continue without an image
        plot_path = ""

    return {"summary": summary, "plot_path": plot_path}
