For a neo-bank context, the most impactful PoC combines **RAG** (for policy/knowledge) and **MCP** (for real-time banking actions). This allows you to evaluate how the AI handles sensitive financial data and switches between "advising" and "executing."

### **The Use Case: "Neo-Bank Smart Dispute & Limit Assistant"**

This assistant handles two high-stakes banking flows in one interface:

1. **RAG (The Knowledge):** Checking the "Terms & Conditions" for transaction disputes (e.g., "What is the time limit to dispute a merchant charge in France?").  
2. **MCP (The Action):** Connecting to the Core Banking System to fetch real-time transaction history or temporarily "freeze" a card if fraud is suspected.

### ---

**1\. Architecture Components**

* **RAG Layer:** A Vector DB (like PGVector) storing your bank's PDF policies on chargebacks, ATM fees, and account tiers.  
* **MCP Tool 1 (Transaction Explorer):** A tool that calls your backend API to list the last 5 transactions for a specific account\_id.  
* **MCP Tool 2 (Limit Manager):** A tool that can update the "Daily Spending Limit" or "Freeze Card" status.  
* **Sentiment Analysis (Advisors):** A Spring AI AroundAdvisor that intercepts the user's message to detect "Urgency/Panic" (e.g., "My card was stolen\!").

### ---

**2\. Why this is perfect for Langfuse Evaluations**

In a neo-bank, you cannot afford "hallucinated policies" or "accidental money transfers." You can use Langfuse to evaluate these **three critical dimensions**:

#### **A. Retrieval Faithfulness (RAG Eval)**

* **The Scenario:** User asks, "Can I get a refund for a 90-day-old transaction?"  
* **Evaluation:** Does the LLM output match the policy retrieved from the Vector DB? If the policy says "60 days" and the LLM says "90 days," Langfuse flags a **Hallucination**.

#### **B. Tool-Call Safety (MCP Eval)**

* **The Scenario:** User says, "I'm worried about my spending, maybe lower my limit?"  
* **Evaluation:** Did the LLM correctly invoke the Limit Manager tool? Did it use the correct parameters (e.g., reducing the limit rather than increasing it)? This is **Trajectory Evaluation** in Langfuse—checking if the "path" the agent took was logical.

#### **C. Regulatory & Tone Compliance**

* **The Scenario:** A frustrated user complaining about a fee.  
* **Evaluation:** Use **LLM-as-a-Judge** in Langfuse to score the response on "Empathy" and "Compliance." Did the AI follow the legal script for fee disclosures?

### ---

**3\. Visualizing the Trace in Langfuse**

When you run this PoC, a single transaction in Langfuse will look like a "Tree":

1. **Input:** "I see a weird charge from 'Paris Bistro', help\!"  
2. **Step 1 (Advisor):** Sentiment Analysis detects "Anxiety."  
3. **Step 2 (MCP):** Tool get\_transactions is called; returns "Paris Bistro \- €45.00."  
4. **Step 3 (RAG):** Searches "Dispute Policy"; finds "Unauthorized merchant charges must be reported within 14 days."  
5. **Output:** "I've found that €45 charge. Since it happened yesterday, you are well within the 14-day limit to dispute it. Would you like me to freeze your card first?"

### ---

**Next Step for your PoC**

Since you're at a neo-bank, security is key. Would you like me to show you how to implement a **"PII Masking Advisor"** in Spring AI so that sensitive data (like full card numbers) is redacted before it even reaches the LLM and gets recorded in Langfuse?