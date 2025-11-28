# 🚌 BuSafe - Ônibus Seguro

![BuSafe](./src/main/resources/static/prototipo/Banner.png)

[cite_start]**BuSafe** é uma solução tecnológica desenvolvida para enfrentar a crise de confiança no transporte público do Espírito Santo[cite: 8, 45]. [cite_start]O sistema utiliza um mapa intuitivo para identificar zonas seguras e perigosas, auxiliando no planejamento de viagens mais seguras[cite: 12].

[cite_start]Este projeto foi apresentado na **XIII Mostra de Projetos Integradores de Extensão** do curso de TADS/SI/Eng. de Software do **UniSales**[cite: 1, 3].

---

## 📱 Funcionalidades e PWA

[cite_start]O sistema foi desenvolvido com um front-end responsivo[cite: 22], funcionando como um **Progressive Web App (PWA)**. Isso permite instalação direta no celular e uma experiência similar a um aplicativo nativo.

* [cite_start]**🗺️ Mapa de Risco:** Visualização de zonas com cores indicativas (Verde para segurança, Vermelho para perigo)[cite: 12, 24].
* [cite_start]**📍 Geolocalização e Rotas:** Exibe rotas de ônibus e a localização do usuário em tempo real, inspirada no aplicativo ÔnibusGV[cite: 24].
* [cite_start]**📢 Relato de Incidentes:** Permite reportar ocorrências (furtos, assaltos) em tempo real, detalhando tipo, linha e local[cite: 12, 25].

---

## 📸 Demonstração Visual

Abaixo estão os protótipos e telas funcionais do sistema:

### 1. Mapa de Rotas e Geolocalização
[cite_start]Visualização ativa das rotas de ônibus utilizando a API do OpenStreetMap (OSM)[cite: 22, 24].

![Protótipo do mapa OSM de rotas com geolocalização ativa](./src/main/resources/static/prototipo/geolocalização.png)
[cite_start]*(Figura 1: Protótipo do mapa OSM de rotas com geolocalização ativa [cite: 32])*

### 2. Áreas de Risco vs. Áreas Seguras
[cite_start]O mapa destaca visualmente as regiões baseadas no histórico de segurança[cite: 12].

![Áreas de risco e áreas seguras](./src/main/resources/static/prototipo/risco.png)
[cite_start]*(Figura 2: Áreas de risco e áreas seguras demarcadas no mapa [cite: 33])*

### 3. Reporte de Ocorrências
[cite_start]Tela dedicada para o usuário inserir dados sobre incidentes ocorridos durante o trajeto[cite: 25].

![Tela de ocorrências](./src/main/resources/static/prototipo/tela%20relato.png)
[cite_start]*(Figura 3: Tela de reporte de um incidente [cite: 42])*

---

## 🛠️ Tecnologias Utilizadas

[cite_start]O desenvolvimento seguiu uma metodologia incremental com gestão via Kanban[cite: 20].

* [cite_start]**Back-end:** Java e Spring Boot[cite: 21].
* [cite_start]**Banco de Dados:** PostgreSQL[cite: 21].
* [cite_start]**Front-end:** HTML, CSS, JavaScript (Responsivo/PWA)[cite: 22].
* [cite_start]**Mapas:** Integração com OpenStreetMap (OSM)[cite: 22].
* [cite_start]**DevOps/Ferramentas:** Docker[cite: 23].

---

## 🔗 Fontes de Informação e Referências

[cite_start]O projeto baseou-se em dados oficiais e pesquisas de percepção pública para justificar sua relevância social[cite: 10, 27]. As principais fontes consultadas foram:

1.  **SESP - Secretaria de Estado da Segurança Pública e Defesa Social**
    * [cite_start]*Dados utilizados:* Painel de furtos e roubos e estatísticas do Observatório de Segurança Pública do Espírito Santo[cite: 10, 52, 53].
    * [Acesse o site da SESP/ES](https://observatorio.sesp.es.gov.br/crimes-contra-o-patrimonio-no-estado-do-espirito-santo)

2.  **NTU - Associação Nacional das Empresas de Transportes Urbanos**
    * [cite_start]*Dados utilizados:* Estudos sobre a percepção de segurança no transporte público urbano[cite: 51].
    * [Acesse o site da NTU](https://www.ntu.org.br/)

---

## 👥 Autores

* [cite_start]**Rayssa Ferreira da Silva** [cite: 5]
* [cite_start]**Luiz Gabriel de Oliveira Ferreira** [cite: 5]
* [cite_start]**Maria Eduarda Fernandes Almeida** [cite: 5]
* [cite_start]**Victor Pimenta Jardim da Silva** [cite: 5]

[cite_start]**Orientador:** Rômulo Ferreira Douro [cite: 6]