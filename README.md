# POPPET
2025 GDG Solution Challenge, POPPET API Git Repository
![poppet](https://github.com/user-attachments/assets/d4bdeec3-33e4-4b5a-adb1-31f6c3769bac)


<br>


## Brief about your solution
<img width="606" alt="image" src="https://github.com/user-attachments/assets/aee7bcce-a5a8-490e-bf36-a446d825f1ec" />



<br><br>

## Opportunities
**Service Name & Tagline** <br>
POPPET ― the AI chat-buddy that helps seniors talk and families listen.

**Problem & Pain Points**(Why we built it) <br>
While talking with my grandma after my grandpa passed away, I hit three roadblocks. 
1) Every conversation eventually led to **worries and concerns**
2) **Repeated the same stories or questions frequently**
3) Felt **sorry for taking time** from her children and grandchildren
4) **Time zone differences** made communication harder with family abroad
   
**Target Users (Personas)**  <br>
- **Older adults** → need a simple, intuitive voice/text UI.
- **Overseas or busy family members** → need asynchronous, concise updates.

**Gap in Existing Solutions**  <br>
AI speakers & social robots (>$1 000, no caregiver digest). Ordinary calls depend on family time and reveal few hidden issues. 

**USP – What makes POPPET different**  <br>
- **Letter-style e-mails**: POPPET auto-summarises chats and sends them to the caregiver on a set cadence.
- **Zero hardware cost**: runs on any smartphone chat app; no extra device purchase.
- **Memory-aware, empathetic chat**: Gemini LLM recalls prior topics and steers fresh, comforting dialogue.
  
**How POPPET Solves the Problem** <br>
Listen → Detect → Deliver
1) Senior chats via voice/text.
2) Google STT + Gemini analyse mood & “unspoken worries.”
3) A concise letter is e-mailed to the caregiver, surfacing issues the senior may hesitate to say aloud.


<br><br>
## List of features offered by the solution
| # | **Current MVP Feature** | Brief Description | 
| --- | --- | --- |
| 1 | **Voice & Text Chat-Buddy** | Natural, empathetic conversation via Google Speech-to-Text, Gemini LLM, and TTS. |
| 2 | **Conversation Memory** | Remembers previous topics to minimise repetitions and keep dialogue fresh. | 
| 3 | **Sentiment & “Unspoken Worry” Detection** | Analyses mood shifts and flags sensitive concerns the user may hesitate to mention. |
| 4 | **Letter-Style Digest E-mail** | Generates concise summaries (daily / every 3 days / weekly) and e-mails them to the caregiver. |
| 5 | **Custom Cadence & Recipients** | Caregiver chooses digest frequency, recipients, and keyword watch-list. |


<br><br>
## Planned post-MVP additions
| Planned Feature | description |
| --- | --- |
| **Urgent Alert Push / Email** | Real-time notification if distress or health-risk keywords are detected. |
| **Multi-language Support** | English UI/voice model after Korean launch, followed by Japanese & others. | 
| **AI Response Tone Selection** | Enables selection of response tone by learning the caregiver’s voice pattern. | 


<br><br>
## Process flow diagram or Use-case diagram
<img width="592" alt="image" src="https://github.com/user-attachments/assets/b1698b73-4018-46d3-8332-202d1bbed192" />


<br><br>
## Wireframes/Mock diagrams of the proposed solution
<img width="628" alt="image" src="https://github.com/user-attachments/assets/0877c977-65b8-43fd-b792-7165b0979f9c" />


<br><br>
## Architecture diagram of the proposed solution
![poppet_system_architecture](https://github.com/user-attachments/assets/608a69b3-b35b-4699-96ba-9d27bfffe746)

**Infrastructure as Code with Terraform** <br>
Terraform scripts provision all core resources—VPC, subnet, service accounts, Cloud SQL instances, and IAM policies—ensuring repeatable, version‑controlled setup.

**CI/CD with GitHub Actions → Artifact Registry** <br>
On every push to the develop branch, GitHub Actions builds the Docker image, tags it with the commit SHA, and pushes it to Google Artifact Registry.
    
**Continuous Delivery on Cloud Run** <br>
Cloud Run is configured for “continuous deployment”: it automatically pulls the latest container image from Artifact Registry, spins up new revisions, and routes traffic after health checks pass.
    
**Managed Database Connectivity** <br>
The Cloud Run service connects to a Cloud SQL (MySQL) instance via the Cloud SQL Auth Proxy, giving the application a secure, private channel to the managed database.


<br><br>
## Technologies to be used in the solution
### 1 . Gemini Spotlight
| What Gemini Does for POPPET | How We Integrate It | Why It Matters |
| --- | --- | --- |
| - Generates **empathetic Korean replies** in real‑time. <br> - Extracts **sentiment & ‘unspoken worries’** from each message. <br> - Creates a **letter‑style JSON digest** (1 / 3 / 7‑day) in a single call. | - Google Generative AI Java SDK inside Spring‑Boot (`GeminiClient`). <br> - JSON Schema mode → deterministic keys (`replyText`, `emotion`, `hiddenWorry`). <br> - API key pulled securely from **Secret Manager** at startup. | - **One API call = 3 tasks** → latency ↓, cost ↓. <br> - Schema output → zero post‑processing errors. <br> - Safety settings tuned for senior‑friendly language. |

### 2 . Supporting Google Cloud Stack

| Layer | Product | Role |
| --- | --- | --- |
| **Speech I/O** | **Cloud Speech‑to‑Text v3**, **Cloud Text‑to‑Speech** | STT for 1‑min audio chunks → text; TTS WaveNet voice for Gemini replies. |
| **Serverless Compute** | **Cloud Run** (Spring‑Boot container, `minInstances = 1`) | Runs REST API & **Spring `@Scheduled`** digest cron; auto‑scales 0 ↔ N. |
| **Database** | **Cloud SQL (MySQL 8)** + Auth Proxy | Stores users, conversation logs, digest timestamps (encrypted channel). |
| **E‑mail** | **Gmail API** (service account) | Sends the Gemini‑generated digest to up to 5 caregivers. |
| **CI / CD** | **GitHub Actions** → **Artifact Registry** → Cloud Run | Build → push Docker image → one‑line `gcloud run deploy`. |
| **Infra as Code** | **Terraform** | VPC, Cloud SQL, Cloud Run, Secret Manager, HTTPS LB, IAM in one `apply`. |
| **Secrets & Security** | **Secret Manager**, HTTPS LB + **Cloud Armor** | API keys & DB creds sealed; TLS termination & DDoS/WAF rules. |
| **Client** | **Flutter** (Dart) | Captures audio, calls `/chat`, receives TTS stream; single code‑base Android/iOS. |





