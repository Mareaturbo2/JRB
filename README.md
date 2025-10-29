# 🏦 JRB - Jalinroubei Bank  

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)  
![React](https://img.shields.io/badge/React-61DBFB?style=for-the-badge&logo=react&logoColor=black)  
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow?style=for-the-badge)  

---

## 📌 Descrição  
O **JRB - Jalinroubei Bank** é um sistema de simulação bancária desenvolvido na disciplina de **Programação Orientada a Objetos (POO)**.  
O projeto foi evoluído para incluir uma **API REST em Java (Spark Framework)** e uma **interface moderna em React (Vite)**.  

O sistema permite:
- 📋 Criação e gerenciamento de contas  
- 💰 Depósitos e saques  
- 📊 Consulta de saldo  
- 🧾 Visualização e exportação de extratos em PDF  

Tudo com persistência local em JSON e geração automática de relatórios PDF no backend.

---

## 👥 Equipe  
- Paulo César Ferreira de Assis - **Mareaturbo2**  
- Robson Sando Andrade Cunha Filho - **DarkArtsBM**  
- Matheus Rangel Kirzner - **mattkirzner**  
- Luiz Henrique da Silva Neves - **Luiz380**  
- Julio Bezerra Coelho - **Juliobzr**  
- Matheus José Cardoso Luna - **matheusluna12**  
- Rafael Farias Santana - **rafaelFarias7**

---

## 🛠 Tecnologias Utilizadas  
### 🔹 Backend  
- Java 17  
- Spark Framework  
- Gson (JSON Parser)  
- iTextPDF (geração de relatórios em PDF)

### 🔹 Frontend  
- React + Vite  
- JavaScript / JSX / CSS  

---

## 🚀 **Como Executar o Projeto JRB**

O projeto JRB é dividido em **duas partes**:
1. 🧠 **Backend** — Java + Spark Framework  
2. 💻 **Frontend** — React + Vite  

A seguir estão os passos para configurar e executar corretamente em qualquer máquina.

---

### ⚙️ **1. Pré-requisitos**

Certifique-se de ter instalados:
- **JDK 17+** → [Baixar](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- **Node.js 18+** → [Baixar](https://nodejs.org/)
- **Git** → [Baixar](https://git-scm.com/)

---

### 📂 **2. Clonar o projeto**

```bash
git clone https://github.com/Mareaturbo2/JRB
cd JRB
```

---

### 🖥️ **3. Executar o Backend (Java + Spark)**

1. Verifique se a pasta `lib/` contém os seguintes arquivos:  
   ```
   itextpdf-5.5.13.3.jar
   gson-2.10.1.jar
   spark-core-2.9.4.jar
   jetty-*.jar
   slf4j-*.jar
   javax.servlet-api-3.1.0.jar
   ```

2. Compile o projeto:
   ```bash
   javac -cp "lib/*;src" -d bin src/app/ApiServer.java
   ```

3. Execute o servidor:
   ```bash
   java -cp "bin;lib/*" app.ApiServer
   ```

4. O console exibirá algo como:
   ```
   [Thread-0] INFO spark.embeddedserver.jetty.EmbeddedJettyServer - >> Listening on 0.0.0.0:8080
   ```

🟢 O backend estará disponível em:
```
http://localhost:8080
```

---

### 🌐 **4. Executar o Frontend (React + Vite)**

1. Entre na pasta do frontend:
   ```bash
   cd src/view/JRB-FRONT
   ```

2. Instale as dependências:
   ```bash
   npm install
   ```

3. Inicie o servidor:
   ```bash
   npm run dev
   ```

4. O terminal mostrará algo como:
   ```
   Local:   http://localhost:5173
   ```

5. Acesse no navegador:
   ```
   http://localhost:5173
   ```
---

### 📄 **5. Testar as Funcionalidades**

#### 🔐 Login e Cadastro
- Crie uma conta pelo front-end  
- Faça login com o CPF e senha cadastrados  

#### 💰 Operações
- **Depósito**: adiciona saldo à conta  
- **Saque**: retira um valor (se houver saldo suficiente)  
- **Saldo**: exibe o valor atual  

#### 📊 Extrato
- Visualize todas as movimentações (data, tipo e valor)  
- Clique em **“Exportar em PDF”**  
  - O PDF é baixado automaticamente no navegador  
  - E salvo também na pasta `/data/extrato_<cpf>.pdf`

---

### 📁 **6. Estrutura do Projeto**

```
JRB/
├── data/                 → Contas e extratos gerados (JSON e PDF)
├── lib/                  → Bibliotecas Java (Spark, Gson, iTextPDF, etc.)
├── src/
│   ├── app/              → ApiServer.java (rotas e endpoints)
│   ├── model/            → Entidades do sistema (Account, Movimentacao, etc.)
│   ├── service/          → Regras de negócio (BankService, ExtratoService)
│   └── view/JRB-FRONT/   → Aplicação React + Vite
├── bin/                  → Classes compiladas do Java
└── README.md             → Documentação do projeto
```

---

### 🧪 **7. Testar pelo Postman (opcional)**

| Método | Rota | Descrição |
|--------|------|------------|
| `POST` | `/api/login` | Login com CPF e senha |
| `POST` | `/api/contas` | Criação de nova conta |
| `PUT`  | `/api/contas/:cpf/deposito?valor=100` | Realizar depósito |
| `PUT`  | `/api/contas/:cpf/saque?valor=50` | Realizar saque |
| `GET`  | `/api/contas/:cpf/extrato` | Retorna extrato em JSON |
| `GET`  | `/api/contas/:cpf/extrato/pdf` | Gera e baixa o extrato em PDF |

---

### ✅ **8. Encerrando o servidor**

Para parar a execução:
- No backend → pressione **Ctrl + C**
- No frontend → pressione **Ctrl + C**

---

### 🧱 **9. Estrutura de Execução Completa**

```
# Backend
javac -cp "lib/*;src" -d bin src/app/ApiServer.java
java -cp "bin;lib/*" app.ApiServer

# Frontend
cd src/view/JRB-FRONT
npm install
npm run dev
```

---

---

## 📌 Trello
[![Trello](https://img.shields.io/badge/Trello-Quadro%20do%20Projeto-0052CC?style=for-the-badge&logo=trello&logoColor=white)](https://trello.com/b/mt2Z0dtN/jrb-projeto-poo)

## 🎨 Protótipo no Figma
[![Figma](https://img.shields.io/badge/Figma-Protótipo%20Lo--Fi-blue?style=for-the-badge&logo=figma)](https://www.figma.com/design/uqrwYchPt0zOBuOFScELYx?node-id=0-1)

## 🎥 Screencast
[![Entrega 1](https://img.shields.io/badge/YouTube-Entrega%201-red?style=for-the-badge&logo=youtube)](https://youtu.be/jlmBgCxwt4k)
[![Entrega 2](https://img.shields.io/badge/YouTube-Entrega%202-red?style=for-the-badge&logo=youtube)](https://youtu.be/koP8LIMcJ9o)
[![Entrega 3](https://img.shields.io/badge/YouTube-Entrega%203-red?style=for-the-badge&logo=youtube)](https://www.youtube.com/watch?v=9Hc3Nm24qgA)

## 🎥 Testes automatizados
[![Entrega 3](https://img.shields.io/badge/YouTube-Entrega%203-red?style=for-the-badge&logo=youtube)](https://www.youtube.com/watch?v=9pXXATPeGEA)

## 🐞 Issue / Bug Tracker
![Issue / Bug Tracker 03](./assets/minha_foto.png)



