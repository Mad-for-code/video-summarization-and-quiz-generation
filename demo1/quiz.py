from transformers import pipeline
import os

print("Generating quiz...")

video_folder = "uploads"

transcript_path = os.path.join(video_folder, "output.txt")
quiz_path = os.path.join(video_folder, "quiz.txt")

with open(transcript_path, "r", encoding="utf-8") as f:
    transcript = f.read()

# use smaller portion of transcript
text = transcript[:1200]

generator = pipeline(
    "text2text-generation",
    model="google/flan-t5-base"
)

quiz_list = []

for i in range(5):

    prompt = f"""
Read the text and create ONE multiple choice question.

Text:
{text}

Rules:
- Question must be based on the text
- Provide exactly 4 options
- Mark the correct answer

Format:

Question:
A)
B)
C)
D)
Answer:
"""

    result = generator(
        prompt,
        max_length=200,
        do_sample=True,
        temperature=0.7
    )

    q = result[0]["generated_text"]

    quiz_list.append(f"Question {i+1}\n{q}\n")

quiz_text = "\n".join(quiz_list)

with open(quiz_path, "w", encoding="utf-8") as f:
    f.write(quiz_text)

print("Quiz generated successfully")