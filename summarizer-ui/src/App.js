import { useState } from "react";

function App() {
  const [text, setText] = useState("");
  const [summary, setSummary] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const summarize = async () => {
    if (loading || !text.trim()) return;

    setLoading(true);
    setError("");
    setSummary("");

    try {
      const response = await fetch(
        "http://localhost:8080/api/summarize",
        {
          method: "POST",
          headers: {
            "Content-Type": "text/plain",
          },
          body: text,
        }
      );

      const data = await response.text();

      if (
        data.startsWith("ERROR:") ||
        data.toLowerCase().includes("error")
      ) {
        setError(data);
      } else {
        setSummary(data);
      }
    } catch (err) {
      setError("Failed to connect to backend");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const clearAll = () => {
    setText("");
    setSummary("");
    setError("");
  };

  return (
    <div
      style={{
        maxWidth: "900px",
        margin: "40px auto",
        background: "#ffffff",
        padding: "30px",
        borderRadius: "12px",
        boxShadow: "0 4px 15px rgba(0,0,0,0.1)",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <h1
        style={{
          textAlign: "center",
          marginBottom: "10px",
        }}
      >
       Text Summarizer
      </h1>

      

      <textarea
        rows="10"
        value={text}
        onChange={(e) => setText(e.target.value)}
        disabled={loading}
        placeholder="Paste your text here..."
        style={{
          width: "100%",
          padding: "12px",
          borderRadius: "8px",
          border: "1px solid #ccc",
          fontSize: "14px",
          boxSizing: "border-box",
        }}
      />

      <br />
      <br />

      <button
        onClick={summarize}
        disabled={loading || !text.trim()}
        style={{
          padding: "10px 20px",
          marginRight: "10px",
          cursor: "pointer",
        }}
      >
        {loading ? "Summarizing..." : "Summarize"}
      </button>

      <button
        onClick={clearAll}
        disabled={loading}
        style={{
          padding: "10px 20px",
          cursor: "pointer",
        }}
      >
        Clear
      </button>

      {error && (
        <div
          style={{
            marginTop: "20px",
            color: "red",
            fontWeight: "bold",
          }}
        >
          Error: {error}
        </div>
      )}

      <h3 style={{ marginTop: "25px" }}>Result</h3>

      <div
        style={{
          border: "1px solid #ccc",
          padding: "15px",
          minHeight: "120px",
          backgroundColor: "#f9f9f9",
          borderRadius: "8px",
          whiteSpace: "pre-wrap",
        }}
      >
        {loading ? "Waiting for response..." : summary}
      </div>
    </div>
  );
}

export default App;