// --- DEFINICIÓN DE CLASES (ENTIDADES) ---

class Usuario {
    constructor(id, nombre, email, password, rol, avatar) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol; // 'admin', 'user', 'anonimo'
        this.avatar = avatar;
    }
}

class Discoteca {
    constructor(id, nombre, direccion, logo) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.logo = logo;
    }
}

class Evento {
    constructor(id, nombre, fecha, discotecaId, imagen, precio, descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.discotecaId = discotecaId; // Relación N:1
        this.imagen = imagen;
        this.precio = precio;
        this.descripcion = descripcion;
    }
}

class Entrada {
    constructor(id, eventoId, usuarioId, qrCode, estado) {
        this.id = id;
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.qrCode = qrCode;
        this.estado = estado; // 'disponible', 'vendida', 'usada'
    }
}

class Transaccion {
    constructor(id, compradorId, entradaId, fecha, monto) {
        this.id = id;
        this.compradorId = compradorId;
        this.entradaId = entradaId;
        this.fecha = fecha;
        this.monto = monto;
    }
}

// --- DATOS MOCK (SIMULACIÓN DE BASE DE DATOS) ---

const discotecas = [
    new Discoteca(1, "Fabrik", "Humanes de Madrid", "https://placehold.co/50x50/000/FFF?text=F"),
    new Discoteca(2, "Kapital", "Calle Atocha", "https://placehold.co/50x50/e74c3c/FFF?text=K")
];

const eventos = [
    new Evento(1, "Opening Summer", "2026-06-15", 1, "https://placehold.co/600x400/2ecc71/FFF?text=Summer", 25.00, "Apertura del verano."),
    new Evento(2, "Techno Night", "2026-02-14", 1, "https://placehold.co/600x400/34495e/FFF?text=Techno", 20.00, "La noche más oscura."),
    new Evento(3, "Fiesta Universitaria", "2026-03-10", 2, "https://placehold.co/600x400/f1c40f/000?text=Uni", 15.00, "Solo para estudiantes.")
];

// --- LÓGICA DE RENDERIZADO (DOM) ---

document.addEventListener('DOMContentLoaded', () => {
    const contenedorEventos = document.getElementById('lista-eventos');

    // Si estamos en index.html y existe el contenedor
    if (contenedorEventos) {
        renderizarEventos(contenedorEventos);
    }
});

function renderizarEventos(contenedor) {
    contenedor.innerHTML = '';
    
    eventos.forEach(evento => {
        // Buscar la discoteca relacionada
        const disco = discotecas.find(d => d.id === evento.discotecaId);
        
        const cardHTML = `
            <div class="col">
                <div class="card h-100 shadow-sm">
                    <img src="${evento.imagen}" class="card-img-top" alt="${evento.nombre}">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-2">
                            <img src="${disco.logo}" class="rounded-circle me-2" width="30">
                            <small class="text-muted fw-bold">${disco.nombre}</small>
                        </div>
                        <h5 class="card-title">${evento.nombre}</h5>
                        <p class="card-text text-truncate">${evento.descripcion}</p>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="badge bg-primary fs-6">${evento.precio}€</span>
                            <small class="text-muted">${evento.fecha}</small>
                        </div>
                    </div>
                    <div class="card-footer bg-white border-top-0">
                        <button class="btn btn-outline-dark w-100">Comprar Entrada</button>
                    </div>
                </div>
            </div>
        `;
        contenedor.innerHTML += cardHTML;
    });
}