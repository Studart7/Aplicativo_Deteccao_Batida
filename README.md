Detecção de Batida 🚗💥
Bem-vindo ao projeto Detecção de Batida! Este aplicativo Android foi desenvolvido para detectar colisões de veículos utilizando os sensores acelerômetro e giroscópio do dispositivo. Ele fornece monitoramento em tempo real e exibe métricas detalhadas para avaliação de desempenho.

Funcionalidades ✨
Detecção em Tempo Real: Monitora dados do acelerômetro e giroscópio para identificar colisões.
Exibição de Matriz de Confusão: Representação visual do desempenho do modelo.
Métricas de Desempenho: Calcula Recall, Precisão, F1 Score e Acurácia.
Alertas ao Usuário: Notifica quando uma colisão é detectada com um temporizador regressivo.
Diálogos Interativos: Permite ao usuário confirmar ou descartar alertas de colisão.
Começando 🚀
Pré-requisitos 🛠
Dispositivo Android com sensores de acelerômetro e giroscópio.
Android Studio instalado em sua máquina de desenvolvimento.
Versão mínima do SDK: definir conforme necessário.
Instalação 📥
Clone o Repositório

git clone https://github.com/nomedousuario/deteccaobatida.git
Abra no Android Studio

Abra o Android Studio.
Selecione File > Open e navegue até o repositório clonado.
Abra o projeto.
Construa o Projeto

Permita que o Android Studio sincronize e construa o projeto.
Resolva quaisquer dependências se solicitado.
Executando o Aplicativo ▶️
Conecte seu dispositivo Android via USB ou inicie um emulador.
Clique no botão Run no Android Studio.
Instale o app em seu dispositivo.
Como Funciona 🔍
O aplicativo utiliza os sensores do dispositivo para monitorar mudanças bruscas indicativas de uma colisão.

Acelerômetro: Detecta desacelerações rápidas.
Giroscópio: Detecta mudanças rotacionais abruptas.
Os dados desses sensores são processados e, se certos limites forem excedidos, o app considera que uma colisão pode ter ocorrido.

Lógica de Detecção de Colisão ⚙️
Se ambos os limites do acelerômetro e giroscópio forem excedidos, uma potencial colisão é detectada.
Um diálogo aparece notificando o usuário e inicia uma contagem regressiva de 15 segundos.
Opções do Usuário:
Confirmar Batida: Atualiza a matriz de confusão como verdadeiro positivo.
Alarme Falso: Descarta o alerta sem atualizar a matriz de confusão.
Se a contagem regressiva expirar sem interação do usuário, o app assume que uma colisão ocorreu e atualiza adequadamente.
Interface do Usuário 🖥
Leituras de Sensores: Exibe valores em tempo real do acelerômetro e giroscópio.
Matriz de Confusão: Mostra o desempenho do algoritmo de detecção.
Exibição de Métricas: Calcula e exibe Recall, Precisão, F1 Score e Acurácia.
Estrutura do Projeto 📁
MainActivity.kt: Atividade principal que lida com eventos de sensores e atualizações de UI.
ConfusionMatrix.kt: Classe que gerencia a matriz de confusão e cálculos de métricas.
SensorService: Serviço que roda em primeiro plano para monitorar dados dos sensores.
Personalização 🛠
Valores de Limite: Ajuste LIMITE_DESACELERACAO e LIMITE_GIRO em MainActivity.kt para calibrar a sensibilidade da detecção.
Contribuindo 🤝
Contribuições são bem-vindas! Por favor, abra issues ou envie pull requests para melhorias.

Licença 📄
Este projeto está licenciado sob a licença MIT.

Agradecimentos 🙏
Agradecimento a todos os colaboradores e à comunidade de código aberto.
Esperamos que você ache este projeto útil e interessante! Se tiver dúvidas ou sugestões, não hesite em entrar em contato. 💬
