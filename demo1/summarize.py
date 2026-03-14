from transformers import pipeline

summarizer = pipeline("summarization")

# read transcript
with open("output.txt", "r", encoding="utf-8") as f:
    text = f.read()

# split text into smaller chunks
max_chunk = 900
chunks = [text[i:i+max_chunk] for i in range(0, len(text), max_chunk)]

final_summary = ""

for chunk in chunks:
    summary = summarizer(chunk, max_length=80, min_length=20, do_sample=False)
    final_summary += summary[0]['summary_text'] + " "

print("\nSummary:\n")
print(final_summary)

with open("summary.txt", "w", encoding="utf-8") as f:
    f.write(final_summary)

print("\n✅ Summary saved to summary.txt")