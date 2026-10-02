(() => {
  const API_BASE = (window.APP_CONFIG?.API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");
  const state = { products: [], laboratories: [], presentations: [], lots: [], filter: "all", query: "", lotQuery: "", productLab: "all", period: 90, setupRequired: false, username: null, busy: false };
  const $ = (selector) => document.querySelector(selector);
  const today = new Date();
  const dateFormatter = new Intl.DateTimeFormat("es-CO", { day: "2-digit", month: "short", year: "numeric" });
  const numberFormatter = new Intl.NumberFormat("es-CO");
  const plainDate = (value) => {
    if (!value) return null;
    const [year, month, day] = String(value).slice(0, 10).split("-").map(Number);
    return Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day) ? new Date(year, month - 1, day) : null;
  };
  const dayDifference = (value) => {
    const date = plainDate(value);
    if (!date) return Number.POSITIVE_INFINITY;
    const start = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    return Math.round((date - start) / 86400000);
  };
  const formatDate = (value) => {
    const date = plainDate(value);
    return date ? dateFormatter.format(date) : "Sin fecha";
  };
  const count = (value) => numberFormatter.format(Number(value) || 0);
  const safe = (value) => String(value ?? "—").replace(/[&<>"']/g, (character) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[character]);
  const icon = (path) => `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${path}</svg>`;
  const actionButton = (action, id, label, path) => `<button class="row-action ${action === "delete-product" ? "danger" : ""}" type="button" data-action="${action}" data-id="${safe(id)}" title="${safe(label)}" aria-label="${safe(label)}">${icon(path)}</button>`;
  const editorDialog = $("#editor-dialog");
  const editorForm = $("#editor-form");
  const editorMessage = $("#editor-message");
  const editPath = "<path d=\"m15 5 4 4M4 20l4-.8L19.2 8a2.1 2.1 0 0 0-3-3L5 16.2 4 20Z\"/>";
  const plusPath = "<path d=\"M12 5v14M5 12h14\"/>";
  const trashPath = "<path d=\"M3 6h18m-2 0-.9 14H5.9L5 6m4 0V4h6v2m-5 4v6m4-6v6\"/>";
  const todayValue = () => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
  };
  const field = (label, name, value = "", options = {}) => {
    const id = `field-${name}`;
    const required = options.required === false ? "" : "required";
    const min = options.min != null ? `min="${safe(options.min)}"` : "";
    const step = options.step ? `step="${options.step}"` : "";
    const placeholder = options.placeholder ? `placeholder="${safe(options.placeholder)}"` : "";
    const help = options.help ? `<small class="field-help">${safe(options.help)}</small>` : "";
    const valueAttr = value === "" || value == null ? "" : `value="${safe(value)}"`;
    if (options.type === "select") {
      const choices = options.items || [];
      const selected = String(value ?? "");
      return `<label class="editor-field" for="${id}">${safe(label)}<select id="${id}" name="${name}" ${required}><option value="">Selecciona una opción</option>${choices.map((item) => `<option value="${safe(item.value)}" ${String(item.value) === selected ? "selected" : ""}>${safe(item.label)}</option>`).join("")}</select>${help}</label>`;
    }
    if (options.type === "textarea") return `<label class="editor-field" for="${id}">${safe(label)}<textarea id="${id}" name="${name}" rows="3" maxlength="5000" ${placeholder}>${safe(value || "")}</textarea>${help}</label>`;
    return `<label class="editor-field" for="${id}">${safe(label)}<input id="${id}" name="${name}" type="${safe(options.type || "text")}" ${valueAttr} ${min} ${step} ${placeholder} ${required} ${options.maxLength ? `maxlength="${options.maxLength}"` : ""}>${help}</label>`;
  };
  const productOptions = () => state.products.map((item) => ({ value: item.idProducto, label: item.nombre }));
  const presentationOptions = () => state.presentations.map((item) => ({
    value: item.idPresentacion,
    label: `${item.producto?.nombre || "Producto"} · ${item.cantidad} ${item.unidadMedida}`
  }));
  const setEditorMessage = (message, isError = false) => {
    editorMessage.textContent = message || "";
    editorMessage.classList.toggle("error", isError);
  };

  function openEditor(type, item = null) {
    if (type === "product" && state.laboratories.length === 0) {
      state.returnToProduct = true;
      openEditor("lab");
      setEditorMessage("Registra el laboratorio primero. Al guardarlo, abriremos el formulario del producto.");
      return;
    }
    if (type === "presentation" && state.products.length === 0) {
      showToast("Primero registra un producto con su presentación inicial.");
      return;
    }
    if (type === "lot" && state.presentations.length === 0) {
      showToast("Registra un producto y una presentación antes de añadir existencias.");
      openEditor("product");
      return;
    }

    const configs = {
      lab: { title: item ? "Editar laboratorio" : "Nuevo laboratorio", description: "Registra el proveedor y la ciudad donde opera.", path: "/laboratorios" },
      product: { title: item ? "Editar producto" : "Nuevo producto", description: item ? "Actualiza su información y presentación principal." : "Crea el producto y su primera presentación en un solo paso.", path: "/productos" },
      presentation: { title: item ? "Editar presentación" : "Nueva presentación", description: "Añade otra forma de presentación al producto seleccionado.", path: "/presentaciones" },
      lot: { title: item ? "Editar lote" : "Registrar lote", description: "Ingresa la cantidad y las fechas del nuevo lote.", path: "/lotes" }
    };
    const config = configs[type];
    if (!config) return;
    editorForm.dataset.type = type;
    editorForm.dataset.id = item?.idLaboratorio ?? item?.idProducto ?? item?.idPresentacion ?? item?.idLote ?? "";
    editorForm.dataset.path = config.path;
    $("#editor-title").textContent = config.title;
    $("#editor-description").textContent = config.description;
    $("#editor-kicker").textContent = item ? "EDITAR REGISTRO" : "NUEVO REGISTRO";
    $("#editor-fields").innerHTML = buildEditorFields(type, item);
    $("#editor-submit").innerHTML = `${icon('<path d="m5 12 4 4L19 6"/>')} ${item ? "Guardar cambios" : "Guardar"}`;
    setEditorMessage("");
    editorDialog.showModal();
  }

  function buildEditorFields(type, item) {
    if (type === "lab") {
      return `<div class="editor-grid">${field("Nombre del laboratorio", "nombre", item?.nombre || "", { maxLength: 100, placeholder: "Ej. Laboratorios del Valle" })}${field("Ciudad", "ciudad", item?.ciudad || "", { maxLength: 100, placeholder: "Ej. Cali" })}</div>`;
    }
    if (type === "product") {
      const first = item && state.presentations.find((entry) => entry.producto?.idProducto === item.idProducto);
      const labItems = state.laboratories.slice().sort((a, b) => a.nombre.localeCompare(b.nombre, "es")).map((lab) => ({ value: lab.idLaboratorio, label: lab.nombre }));
      return `<div class="editor-grid">${field("Nombre del producto", "nombre", item?.nombre || "", { maxLength: 150, placeholder: "Ej. Acetaminofén" })}${field("Laboratorio", "idLaboratorio", item?.laboratorio?.idLaboratorio, { type: "select", items: labItems })}${field("Descripción", "descripcion", item?.descripcion || "", { type: "textarea", required: false, placeholder: "Información opcional del producto" })}<div class="editor-section-title"><strong>Presentación principal</strong><span>Ej. 20 tabletas por caja</span></div>${field("Unidades por presentación", "cantidadPresentacion", first?.cantidad ?? "", { type: "number", min: 1, step: 1, placeholder: "20", required: !item || Boolean(first) })}${field("Unidad de medida", "unidadMedida", first?.unidadMedida || "", { maxLength: 100, placeholder: "Tabletas, ml, cápsulas…", required: !item || Boolean(first), help: "Puedes escribir la unidad que uses." })}${field("Precio por presentación", "precio", first?.precio ?? "", { type: "number", min: 0, step: "0.01", placeholder: "0", required: !item || Boolean(first) })}</div>${!item ? '<p class="form-note">El producto y esta presentación se guardan juntos. Podrás registrar más presentaciones después.</p>' : ""}`;
    }
    if (type === "presentation") {
      return `<div class="editor-grid">${field("Producto", "idProducto", item?.producto?.idProducto, { type: "select", items: productOptions() })}${field("Unidades por presentación", "cantidad", item?.cantidad ?? "", { type: "number", min: 1, step: 1, placeholder: "20" })}${field("Unidad de medida", "unidadMedida", item?.unidadMedida || "", { maxLength: 100, placeholder: "Tabletas, ml, cápsulas…", help: "Puedes escribir la unidad que uses." })}${field("Precio", "precio", item?.precio ?? "", { type: "number", min: 0, step: "0.01", placeholder: "0" })}</div>`;
    }
    const dates = `<div class="editor-grid">${field("Fecha de ingreso", "fechaIngreso", item?.fechaIngreso || todayValue(), { type: "date" })}${field("Fecha de vencimiento", "fechaVencimiento", item?.fechaVencimiento || "", { type: "date" })}${field("Cantidad disponible", "cantidad", item?.cantidad ?? "", { type: "number", min: 1, step: 1, placeholder: "0" })}${field("Presentación", "idPresentacion", item?.presentacion?.idPresentacion, { type: "select", items: presentationOptions() })}</div><p class="form-note">La fecha de vencimiento debe ser igual o posterior a la fecha de ingreso.</p>`;
    return dates;
  }

  function showToast(message, isError = false) {
    const toast = $("#app-toast");
    toast.textContent = message;
    toast.classList.toggle("error", isError);
    toast.classList.add("visible");
    window.clearTimeout(state.toastTimer);
    state.toastTimer = window.setTimeout(() => toast.classList.remove("visible"), 3600);
  }

  async function writeApi(method, path, payload) {
    const response = await fetch(`${API_BASE}${path}`, {
      method,
      credentials: "include",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify(payload)
    });
    const result = await response.json().catch(() => ({}));
    if (response.status === 401) {
      showAuth("Tu sesión terminó. Inicia sesión de nuevo para continuar.");
      const error = new Error("Tu sesión terminó.");
      error.authRequired = true;
      throw error;
    }
    if (!response.ok) throw new Error(result.message || "No se pudo guardar el registro.");
    return result;
  }

  async function saveEditor(event) {
    event.preventDefault();
    if (state.saving) return;
    if (!editorForm.reportValidity()) return;
    const data = new FormData(editorForm);
    const type = editorForm.dataset.type;
    const id = editorForm.dataset.id;
    const path = editorForm.dataset.path;
    const numberOrNull = (value) => value === "" ? null : Number(value);
    let payload;
    if (type === "lab") payload = { nombre: data.get("nombre"), ciudad: data.get("ciudad") };
    if (type === "product") payload = {
      nombre: data.get("nombre"), descripcion: data.get("descripcion"), idLaboratorio: numberOrNull(data.get("idLaboratorio")),
      cantidadPresentacion: numberOrNull(data.get("cantidadPresentacion")), unidadMedida: data.get("unidadMedida") || null, precio: numberOrNull(data.get("precio"))
    };
    if (type === "presentation") payload = { cantidad: numberOrNull(data.get("cantidad")), unidadMedida: data.get("unidadMedida"), precio: numberOrNull(data.get("precio")), idProducto: numberOrNull(data.get("idProducto")) };
    if (type === "lot") payload = { fechaIngreso: data.get("fechaIngreso"), fechaVencimiento: data.get("fechaVencimiento"), cantidad: numberOrNull(data.get("cantidad")), idPresentacion: numberOrNull(data.get("idPresentacion")) };
    if (type === "lot" && payload.fechaVencimiento < payload.fechaIngreso) {
      setEditorMessage("El vencimiento no puede ser anterior al ingreso.", true);
      $("#field-fechaVencimiento").focus();
      return;
    }
    if (type === "product" && id && !payload.cantidadPresentacion && !payload.unidadMedida && payload.precio == null) {
      delete payload.cantidadPresentacion;
      delete payload.unidadMedida;
      delete payload.precio;
    }
    state.saving = true;
    $("#editor-submit").disabled = true;
    $("#editor-submit").textContent = "Guardando…";
    setEditorMessage("");
    try {
      const method = id ? "PUT" : "POST";
      const result = await writeApi(method, id ? `${path}/${id}` : path, payload);
      const reopenProduct = type === "lab" && state.returnToProduct;
      state.returnToProduct = false;
      await loadData();
      editorDialog.close();
      const names = { lab: "El laboratorio", product: "El producto", presentation: "La presentación", lot: "El lote" };
      showToast(`${names[type]} ${id ? "se actualizó" : "se guardó"} correctamente.`);
      if (reopenProduct) {
        openEditor("product", null);
        $("#field-idLaboratorio").value = String(result.idLaboratorio);
      }
      if (type === "lab" && state.pendingLabSearch) {
        $("#laboratory-filter").value = String(result.idLaboratorio);
        state.productLab = String(result.idLaboratorio);
        state.pendingLabSearch = false;
        renderProducts();
      }
    } catch (error) {
      if (!error.authRequired) setEditorMessage(error.message || "No se pudo guardar. Revisa los datos e inténtalo otra vez.", true);
    } finally {
      state.saving = false;
      $("#editor-submit").disabled = false;
      $("#editor-submit").innerHTML = `${icon('<path d="m5 12 4 4L19 6"/>')} Guardar`;
    }
  }
  const initials = (value) => String(value || "?").trim().split(/\s+/).slice(0, 2).map((part) => part[0] || "").join("").toUpperCase();
  const getProduct = (lot) => lot?.presentacion?.producto || {};
  const getPresentation = (lot) => lot?.presentacion || {};
  const lotName = (lot) => getProduct(lot).nombre || "Producto sin nombre";
  const statusFor = (lot) => {
    const days = dayDifference(lot.fechaVencimiento);
    if (days < 0) return { text: "Vencido", className: "expired", days };
    if (days <= 30) return { text: days === 0 ? "Vence hoy" : `En ${days} días`, className: "expired", days };
    if (days <= 90) return { text: `En ${days} días`, className: "", days };
    return { text: "Vigente", className: "ok", days };
  };
  const setConnection = (connected, message = "Conectado") => {
    const pill = $("#connection-pill");
    pill.classList.toggle("offline", !connected);
    pill.querySelector("span").textContent = message;
  };
  const fetchList = async (path) => {
    const response = await fetch(`${API_BASE}${path}`, { headers: { Accept: "application/json" }, cache: "no-store", credentials: "include" });
    if (response.status === 401) {
      const error = new Error("La sesión terminó.");
      error.authRequired = true;
      throw error;
    }
    if (!response.ok) throw new Error(`${path} respondió ${response.status}`);
    const data = await response.json();
    if (!Array.isArray(data)) throw new Error(`${path} no devolvió una lista válida`);
    return data;
  };

  function renderStats() {
    const upcoming = state.lots.filter((lot) => {
      const days = dayDifference(lot.fechaVencimiento);
      return days >= 0 && days <= state.period;
    });
    const expired = state.lots.filter((lot) => dayDifference(lot.fechaVencimiento) < 0);
    const units = state.lots.reduce((sum, lot) => sum + (Number(lot.cantidad) || 0), 0);
    const riskUnits = upcoming.reduce((sum, lot) => sum + (Number(lot.cantidad) || 0), 0);
    $("#stat-products").textContent = count(state.products.length);
    $("#stat-lots").textContent = count(state.lots.length);
    $("#stat-units").textContent = count(units);
    $("#stat-expiring").textContent = count(upcoming.length);
    $("#stat-expiring-label").textContent = `Por vencer · ${state.period} días`;
    $("#stat-expired").textContent = count(expired.length);
    $("#nav-product-count").textContent = count(state.products.length);
    $("#summary-count").textContent = count(upcoming.length);
    $("#summary-units").textContent = count(riskUnits);
    $("#distribution-total").textContent = count(state.products.length);
    const healthy = state.lots.length - upcoming.length - expired.length;
    $("#chart-lots").textContent = count(state.lots.length);
    $("#legend-ok").textContent = count(healthy);
    $("#legend-risk").textContent = count(upcoming.length + expired.length);
    const healthyShare = state.lots.length ? Math.round((healthy / state.lots.length) * 100) : 0;
    $(".chart-orbit").style.background = `conic-gradient(#33885b 0 ${healthyShare}%, #b9dec4 ${healthyShare}% 100%)`;
    $(".distribution-chart").setAttribute("aria-label", `${count(healthy)} lotes al día y ${count(upcoming.length + expired.length)} lotes para revisar`);
    $("#summary-period").textContent = String(state.period);
  }

  function renderMiniList() {
    const target = $("#mini-expiry-list");
    const attention = state.lots.filter((lot) => dayDifference(lot.fechaVencimiento) <= state.period)
      .sort((a, b) => dayDifference(a.fechaVencimiento) - dayDifference(b.fechaVencimiento)).slice(0, 4);
    if (!attention.length) {
      target.innerHTML = '<div class="empty-state">No hay lotes vencidos ni próximos a vencer. ¡Todo en orden!</div>';
      return;
    }
    target.innerHTML = attention.map((lot) => {
      const status = statusFor(lot);
      const product = lotName(lot);
      return `<div class="mini-item"><div class="mini-product"><span class="product-avatar">${safe(initials(product))}</span><span><strong>${safe(product)}</strong><small>Lote #${safe(lot.idLote)} · ${count(lot.cantidad)} unidades</small></span></div><span class="mini-date">${safe(formatDate(lot.fechaVencimiento))}</span><span class="status-badge ${status.className}">${safe(status.text)}</span></div>`;
    }).join("");
  }

  function renderProducts() {
    const query = state.query.trim().toLocaleLowerCase("es-CO");
    const filtered = state.products.filter((product) => {
      const laboratory = product.laboratorio?.nombre || "";
      const matchesQuery = `${product.nombre || ""} ${product.descripcion || ""} ${laboratory}`.toLocaleLowerCase("es-CO").includes(query);
      const matchesLab = state.productLab === "all" || String(product.laboratorio?.idLaboratorio) === state.productLab;
      return matchesQuery && matchesLab;
    });
    const body = $("#products-table");
    $("#product-result-count").textContent = `${count(filtered.length)} ${filtered.length === 1 ? "producto" : "productos"}`;
    $("#products-footer").textContent = query ? `Resultados para “${state.query}”` : `Mostrando ${count(filtered.length)} productos registrados`;
    if (!filtered.length) {
      body.innerHTML = `<tr><td colspan="6" class="empty-cell">${state.products.length ? "No encontramos productos con esa búsqueda." : `Aún no hay productos en el inventario. <button class="inline-action" type="button" data-action="new-product">Crear el primer producto</button>`}</td></tr>`;
      return;
    }
    body.innerHTML = filtered.map((product) => {
      const presentations = state.presentations.filter((item) => item.producto?.idProducto === product.idProducto);
      const presentationLabel = presentations.length ? presentations.map((item) => `${item.cantidad ?? ""} ${item.unidadMedida || ""}`.trim()).join(", ") : "Sin presentaciones";
      const stock = state.lots.filter((lot) => getProduct(lot).idProducto === product.idProducto).reduce((sum, lot) => sum + (Number(lot.cantidad) || 0), 0);
      return `<tr><td><div class="table-product"><span class="product-avatar">${safe(initials(product.nombre))}</span><span><strong>${safe(product.nombre)}</strong><small>Referencia #${safe(product.idProducto)}</small></span></div></td><td>${safe(product.laboratorio?.nombre || "Sin laboratorio")}</td><td>${safe(presentationLabel)}</td><td><strong class="stock-value">${count(stock)}</strong> <span class="cell-muted">unidades</span></td><td class="cell-muted">${safe(product.descripcion || "Sin descripción")}</td><td><div class="row-actions">${actionButton("edit-product", product.idProducto, "Editar producto", editPath)}${actionButton("add-presentation", product.idProducto, "Añadir presentación", plusPath)}${actionButton("delete-product", product.idProducto, "Eliminar producto", trashPath)}</div></td></tr>`;
    }).join("");
  }

  function renderLaboratories() {
    const target = $("#laboratories-table");
    $("#laboratory-result-count").textContent = `${count(state.laboratories.length)} ${state.laboratories.length === 1 ? "laboratorio" : "laboratorios"}`;
    $("#laboratories-footer").textContent = `Asociados a ${count(state.products.length)} productos registrados`;
    if (!state.laboratories.length) {
      target.innerHTML = '<tr><td colspan="4" class="empty-cell">Aún no hay laboratorios. <button class="inline-action" type="button" data-action="new-lab">Registrar el primero</button></td></tr>';
      return;
    }
    target.innerHTML = state.laboratories.slice().sort((a, b) => String(a.nombre || "").localeCompare(String(b.nombre || ""), "es"))
      .map((laboratory) => {
        const products = state.products.filter((product) => product.laboratorio?.idLaboratorio === laboratory.idLaboratorio).length;
        return `<tr><td><div class="table-product"><span class="product-avatar laboratory-avatar"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 3h6m-5 0v7l-5.5 8.4A2 2 0 0 0 6.2 21h11.6a2 2 0 0 0 1.7-2.6L14 10V3"/><path d="M8 16h8"/></svg></span><span><strong>${safe(laboratory.nombre)}</strong><small>Laboratorio #${safe(laboratory.idLaboratorio)}</small></span></div></td><td>${safe(laboratory.ciudad || "Sin ciudad")}</td><td>${count(products)} ${products === 1 ? "producto" : "productos"}</td><td><div class="row-actions">${actionButton("edit-lab", laboratory.idLaboratorio, "Editar laboratorio", editPath)}</div></td></tr>`;
      }).join("");
  }

  function renderLots() {
    const target = $("#lots-table");
    const filtered = getVisibleLots();
    $("#lots-footer").textContent = `${count(filtered.length)} lotes · Información de consulta`;
    if (!filtered.length) {
      target.innerHTML = `<tr><td colspan="7" class="empty-cell">${state.filter === "expired" ? "No hay lotes vencidos." : state.filter === "soon" ? `No hay lotes por vencer en los próximos ${state.period} días.` : state.presentations.length ? `Todavía no has registrado existencias. <button class="inline-action" type="button" data-action="new-lot">Registrar el primer lote</button>` : "Crea un producto con presentación para comenzar a registrar lotes."}</td></tr>`;
      return;
    }
    target.innerHTML = filtered.map((lot) => {
      const product = lotName(lot);
      const presentation = getPresentation(lot);
      const status = statusFor(lot);
      const presentationText = `${presentation.cantidad ?? ""} ${presentation.unidadMedida || ""}`.trim() || "Sin presentación";
      return `<tr><td><div class="table-product"><span class="product-avatar">${safe(initials(product))}</span><span><strong>${safe(product)}</strong><small>Lote #${safe(lot.idLote)}</small></span></div></td><td>${safe(presentationText)}</td><td>${safe(formatDate(lot.fechaIngreso))}</td><td>${safe(formatDate(lot.fechaVencimiento))}</td><td><strong>${count(lot.cantidad)}</strong></td><td><span class="status-badge ${status.className}">${safe(status.text)}</span></td><td><div class="row-actions">${actionButton("edit-lot", lot.idLote, "Editar lote", editPath)}</div></td></tr>`;
    }).join("");
  }

  function render() {
    renderStats();
    renderMiniList();
    renderProducts();
    renderLaboratories();
    renderLots();
  }

  function populateLaboratoryFilter() {
    const select = $("#laboratory-filter");
    const selected = state.productLab;
    select.innerHTML = '<option value="all">Todos los laboratorios</option>' + state.laboratories
      .slice().sort((a, b) => String(a.nombre || "").localeCompare(String(b.nombre || ""), "es"))
      .map((laboratory) => `<option value="${safe(laboratory.idLaboratorio)}">${safe(laboratory.nombre)}</option>`).join("");
    select.value = selected;
  }

  function getVisibleLots() {
    const query = state.lotQuery.trim().toLocaleLowerCase("es-CO");
    return state.lots.filter((lot) => {
      const days = dayDifference(lot.fechaVencimiento);
      const text = `${lotName(lot)} ${lot.idLote ?? ""} ${getProduct(lot).laboratorio?.nombre || ""}`.toLocaleLowerCase("es-CO");
      if (!text.includes(query)) return false;
      if (state.filter === "expired") return days < 0;
      if (state.filter === "soon") return days >= 0 && days <= state.period;
      return true;
    }).sort((a, b) => dayDifference(a.fechaVencimiento) - dayDifference(b.fechaVencimiento));
  }

  function csvCell(value) {
    let text = String(value ?? "").replace(/[\r\n]+/g, " ");
    if (/^[=+@\-]/.test(text)) text = `'${text}`;
    return `"${text.replace(/"/g, '""')}"`;
  }

  function downloadCsv(filename, rows) {
    const csv = "\uFEFF" + rows.map((row) => row.map(csvCell).join(";")).join("\r\n");
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  async function loadData() {
    const refresh = $("#refresh-button");
    const notice = $("#api-notice");
    refresh.disabled = true;
    try {
      const [products, laboratories, presentations, lots] = await Promise.all([
        fetchList("/productos"), fetchList("/laboratorios"), fetchList("/presentaciones"), fetchList("/lotes")
      ]);
      state.products = products;
      state.laboratories = laboratories;
      state.presentations = presentations;
      state.lots = lots;
      populateLaboratoryFilter();
      render();
      setConnection(true);
      notice.classList.add("hidden");
      $("#today").textContent = dateFormatter.format(new Date());
    } catch (error) {
      if (error.authRequired) {
        showAuth("Tu sesión terminó. Inicia sesión de nuevo para continuar.");
        return;
      }
      console.error("No se pudo consultar el backend:", error);
      setConnection(false, "Sin conexión");
      $("#api-error").textContent = `No se pudo consultar ${API_BASE}. Verifica que Spring Boot esté activo y que el backend permita solicitudes desde el origen del frontend (CORS).`;
      notice.classList.remove("hidden");
      render();
    } finally {
      refresh.disabled = false;
    }
  }

  async function deleteProduct(product) {
    if (!product) return;
    const presentations = state.presentations.filter((item) => item.producto?.idProducto === product.idProducto);
    const presentationIds = new Set(presentations.map((item) => item.idPresentacion));
    const lots = state.lots.filter((item) => presentationIds.has(item.presentacion?.idPresentacion));
    const stock = lots.reduce((total, lot) => total + (Number(lot.cantidad) || 0), 0);
    const details = lots.length
      ? `\n\nTambién se eliminarán ${count(presentations.length)} ${presentations.length === 1 ? "presentación" : "presentaciones"}, ${count(lots.length)} ${lots.length === 1 ? "lote" : "lotes"} y ${count(stock)} unidades registradas.`
      : `\n\nTambién se eliminarán ${count(presentations.length)} ${presentations.length === 1 ? "presentación" : "presentaciones"}.`;
    if (!window.confirm(`¿Eliminar “${product.nombre}” de forma permanente?${details}\n\nEsta acción no se puede deshacer.`)) return;
    try {
      await writeApi("DELETE", `/productos/${product.idProducto}`);
      state.products = state.products.filter((item) => item.idProducto !== product.idProducto);
      state.presentations = state.presentations.filter((item) => item.producto?.idProducto !== product.idProducto);
      state.lots = state.lots.filter((item) => !presentationIds.has(item.presentacion?.idPresentacion));
      populateLaboratoryFilter();
      render();
      showToast(`Se eliminó “${product.nombre}” y sus datos asociados.`);
    } catch (error) {
      showToast(error.message || "No se pudo eliminar el producto.", true);
    }
  }

  function setAuthMessage(message, isError = false) {
    const target = $("#auth-message");
    target.textContent = message || "";
    target.classList.toggle("error", isError);
  }

  function showAuth(message = "") {
    document.body.classList.remove("authenticated");
    if (state.username) setAuthMessage(message || "Tu sesión se cerró.");
    state.username = null;
  }

  function applyAuth(username) {
    state.username = username;
    document.body.classList.add("authenticated");
    $("#profile-user").textContent = username;
    $("#profile-avatar").textContent = initials(username);
    setAuthMessage("");
    loadData();
  }

  function updateAuthForm(setupRequired) {
    state.setupRequired = Boolean(setupRequired);
    $("#auth-kicker").textContent = state.setupRequired ? "CONFIGURACIÓN INICIAL" : "TU INVENTARIO, EN EQUILIBRIO";
    $("#auth-title").textContent = state.setupRequired ? "Crea tu cuenta" : "Inicia sesión";
    $("#auth-copy").textContent = state.setupRequired
      ? "Crea la primera cuenta para proteger el acceso a tu inventario."
      : "Ingresa a tu espacio para consultar productos y vencimientos.";
    $("#auth-submit").innerHTML = `${state.setupRequired ? "Crear cuenta e ingresar" : "Iniciar sesión"} <svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6"/></svg>`;
    $("#auth-password").autocomplete = state.setupRequired ? "new-password" : "current-password";
  }

  async function checkSession() {
    try {
      const response = await fetch(`${API_BASE}/auth/status`, { cache: "no-store", credentials: "include" });
      const status = await response.json();
      updateAuthForm(status.setupRequired);
      if (status.authenticated) applyAuth(status.username);
    } catch (error) {
      updateAuthForm(false);
      setAuthMessage(`No se pudo conectar con ${API_BASE}. Inicia el backend y revisa la configuración CORS.`, true);
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    if (state.busy) return;
    const username = $("#auth-username").value.trim();
    const password = $("#auth-password").value;
    const button = $("#auth-submit");
    state.busy = true;
    button.disabled = true;
    button.textContent = state.setupRequired ? "Creando cuenta…" : "Verificando…";
    setAuthMessage("");
    try {
      const endpoint = state.setupRequired ? "/auth/register" : "/auth/login";
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({ username, password })
      });
      const result = await response.json();
      if (!response.ok) {
        updateAuthForm(result.setupRequired);
        setAuthMessage(result.message || "No se pudo iniciar sesión.", true);
        return;
      }
      $("#auth-password").value = "";
      applyAuth(result.username);
    } catch (error) {
      setAuthMessage(`No se pudo conectar con ${API_BASE}. Comprueba que el backend esté activo.`, true);
    } finally {
      state.busy = false;
      button.disabled = false;
      button.innerHTML = `${state.setupRequired ? "Crear cuenta e ingresar" : "Iniciar sesión"} <svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6"/></svg>`;
    }
  }

  $("#today").textContent = dateFormatter.format(new Date());
  $("#auth-form").addEventListener("submit", submitAuth);
  editorForm.addEventListener("submit", saveEditor);
  $("#new-product-cta").addEventListener("click", () => openEditor("product"));
  $("#new-laboratory-button").addEventListener("click", () => openEditor("lab"));
  $("#new-presentation-button").addEventListener("click", () => openEditor("presentation"));
  $("#new-lot-button").addEventListener("click", () => openEditor("lot"));
  $("#editor-close").addEventListener("click", () => editorDialog.close());
  $("#editor-cancel").addEventListener("click", () => editorDialog.close());
  editorDialog.addEventListener("click", (event) => {
    if (event.target === editorDialog) editorDialog.close();
  });
  document.addEventListener("click", (event) => {
    const button = event.target.closest("[data-action]");
    if (!button) return;
    const id = Number(button.dataset.id);
    const actions = {
      "new-product": () => openEditor("product"),
      "new-lab": () => openEditor("lab"),
      "new-lot": () => openEditor("lot"),
      "edit-product": () => openEditor("product", state.products.find((item) => item.idProducto === id)),
      "delete-product": () => deleteProduct(state.products.find((item) => item.idProducto === id)),
      "add-presentation": () => openEditor("presentation", { producto: state.products.find((item) => item.idProducto === id) }),
      "edit-lab": () => openEditor("lab", state.laboratories.find((item) => item.idLaboratorio === id)),
      "edit-lot": () => openEditor("lot", state.lots.find((item) => item.idLote === id))
    };
    actions[button.dataset.action]?.();
  });
  $("#logout-button").addEventListener("click", async () => {
    try {
      await fetch(`${API_BASE}/auth/logout`, { method: "POST", credentials: "include" });
    } finally {
      state.products = [];
      state.laboratories = [];
      state.presentations = [];
      state.lots = [];
      showAuth("Sesión cerrada correctamente.");
      checkSession();
    }
  });
  $("#refresh-button").addEventListener("click", loadData);
  $("#notice-retry").addEventListener("click", loadData);
  $("#product-search").addEventListener("input", (event) => {
    state.query = event.target.value;
    renderProducts();
  });
  $("#laboratory-filter").addEventListener("change", (event) => {
    state.productLab = event.target.value;
    renderProducts();
  });
  $("#lot-search").addEventListener("input", (event) => {
    state.lotQuery = event.target.value;
    renderLots();
  });
  document.querySelectorAll(".period-button").forEach((button) => button.addEventListener("click", () => {
    document.querySelectorAll(".period-button").forEach((item) => item.classList.toggle("active", item === button));
    state.period = Number(button.dataset.days) || 90;
    renderStats();
    renderMiniList();
    renderLots();
  }));
  $("#export-products").addEventListener("click", () => {
    const products = state.products.filter((product) => {
      const lab = product.laboratorio?.nombre || "";
      return `${product.nombre || ""} ${product.descripcion || ""} ${lab}`.toLocaleLowerCase("es-CO").includes(state.query.trim().toLocaleLowerCase("es-CO"))
        && (state.productLab === "all" || String(product.laboratorio?.idLaboratorio) === state.productLab);
    });
    const rows = [["ID", "Producto", "Laboratorio", "Presentaciones", "Descripción"]];
    products.forEach((product) => {
      const presentations = state.presentations.filter((item) => item.producto?.idProducto === product.idProducto).map((item) => `${item.cantidad ?? ""} ${item.unidadMedida || ""} · ${item.precio ?? ""}`).join(" | ");
      rows.push([product.idProducto, product.nombre, product.laboratorio?.nombre, presentations, product.descripcion]);
    });
    downloadCsv("productos-inventario.csv", rows);
  });
  $("#export-lots").addEventListener("click", () => {
    const rows = [["ID lote", "Producto", "Laboratorio", "Presentación", "Ingreso", "Vencimiento", "Cantidad", "Estado"]];
    getVisibleLots().forEach((lot) => rows.push([
      lot.idLote, lotName(lot), getProduct(lot).laboratorio?.nombre,
      `${getPresentation(lot).cantidad ?? ""} ${getPresentation(lot).unidadMedida || ""}`,
      lot.fechaIngreso, lot.fechaVencimiento, lot.cantidad, statusFor(lot).text
    ]));
    downloadCsv("lotes-inventario.csv", rows);
  });
  $("#print-report").addEventListener("click", () => window.print());
  document.querySelectorAll(".filter-tab").forEach((button) => button.addEventListener("click", () => {
    document.querySelectorAll(".filter-tab").forEach((tab) => tab.classList.toggle("active", tab === button));
    state.filter = button.dataset.filter;
    renderLots();
  }));
  document.querySelectorAll(".nav-link").forEach((link) => link.addEventListener("click", () => {
    document.querySelectorAll(".nav-link").forEach((item) => item.classList.toggle("active", item === link));
    $("#sidebar").classList.remove("open");
  }));
  $("#menu-toggle").addEventListener("click", () => $("#sidebar").classList.toggle("open"));
  checkSession();
})();
