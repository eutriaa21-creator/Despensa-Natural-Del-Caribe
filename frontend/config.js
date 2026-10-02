// En local, usa Spring Boot en localhost. En una web publicada, no intentes
// conectarte al localhost del visitante: configura aquí la URL pública del API.
const localHosts = new Set(["localhost", "127.0.0.1", "::1"]);
const isLocalDevelopment = localHosts.has(window.location.hostname);

window.APP_CONFIG = {
  API_BASE_URL: isLocalDevelopment ? "http://localhost:8080" : ""
};
