# SearchEngine: Built From Scratch

> An educational project to understand how modern search engines work, focusing on semantic search, web crawling, and vector embeddings.

## Key Features

- **Intelligent Web Crawling**: Configurable depth and domain filtering
- **Semantic Understanding**: Uses AI embeddings that capture meaning, not just keywords
- **Natural Language Queries**: Search using conversational language
- **Vector-Based Retrieval**: Returns results ranked by semantic similarity

## Architecture

The system consists of four main components:
![Architecture](src/main/resources/Architecture%20Diagram%20Example%20-%20Multiplayer%20(Community)%20(1).png)
1. **Web Crawler**: Extracts content from websites starting from seed URLs
2. **Embedding Generator**:
    - Converts text to vector embeddings using Ollama models
    - Supports different embedding models (default: `nomic-embed-text`)
3. **Elasticsearch Storage**: Stores documents with their vector representations
4. **Semantic Search Engine**: Performs vector similarity search for natural language queries
    - Takes natural language queries as input
    - Converts queries to vector embeddings
    - Returns ranked results based on semantic similarity


## How It Works

### Crawling Flow
1. Start with a seed URL (default or user-provided)
2. Extract text content using JSoup HTML parsing
3. Generate vector embeddings for the text
4. Store document with embeddings in Elasticsearch
5. Extract all links from the page
6. Queue new discovered URLs for processing
7. Continue until depth limit is reached or queue is empty

### Search Flow
1. User enters a natural language query
2. System generates an embedding vector for the query
3. Query vector is compared to all document vectors in Elasticsearch
4. Documents are ranked by cosine similarity (semantic relevance)
5. Results above the similarity threshold are returned to the user



## Setup & Requirements

### Prerequisites
- Java 21 or higher
- Docker and Docker Compose

### Docker Containers
The system relies on the following Docker containers:

### Elasticsearch (`elasticsearch:8.5.3`)
- Primary vector database for document storage
- Port: 9200
- Environment:
    - Single-node discovery
    - Security features disabled for development

### Kibana (`kibana:8.5.3`)
- Web interface for Elasticsearch management
- Port: 5601
- Features:
    - Index management
    - Search analytics
    - System monitoring dashboards

### Ollama (`ollama/ollama`)
- Local AI model server for generating embeddings
- Port: 11434
- Pre-loaded models:
    - `nomic-embed-text` (default)
    - `all-minilm` (alternative)

### Quick Start

1. Clone the repository:
```bash
git clone https://github.com/yourusername/semantic-search.git
cd semantic-search
```

2. Start required services:
```bash
docker-compose up -d
```

3. Build and run the application:
```bash
mvn clean package
java -jar target/semanticsearch-1.0-SNAPSHOT.jar
```

### Configuration
System configuration is stored in `config.properties`:
- `max_Depth`: Maximum crawl depth
- `blocked_Domains`: Comma-separated list of domains to avoid
- `index_db`: Elasticsearch index name
- `base_url`: Default URL to crawl
- `ollama_host`: Ollama API endpoint



## Technologies

- **Java**: Core application language
- **Elasticsearch**: Vector storage and retrieval
- **Ollama**: Local AI model server for embeddings
- **JSoup**: HTML parsing and web crawling
- **Docker**: Containerization for dependencies