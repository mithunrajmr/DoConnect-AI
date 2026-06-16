import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as THREE from 'three';
import '../styles/LandingPage.css';

export default function LandingPage() {
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const cleanupRef = useRef(false);

    useEffect(() => {
        if (isAuthenticated) {
            navigate('/feed', { replace: true });
        }
    }, [isAuthenticated, navigate]);

    useEffect(() => {
        if (isAuthenticated || cleanupRef.current) return;
        cleanupRef.current = true;

        let animationFrameId;
        let waitInterval;
        let scrollSpacer;
        
        const navigateToLogin = () => navigate('/login');
        const navigateToRegister = () => navigate('/register');

        // ==== INJECTED MAIN.JS ====
// Value-Driven Content Array mapping Technical Features -> Human Outcomes
const portfolioData = [
    {
        tag: "01 // THE FRICTION",
        title: "Signal. Not Noise.",
        subtitle: "The AI-Native Knowledge Platform",
        body: "Traditional developer forums are broken. Fragmented answers, duplicate threads, and toxic comments kill collaboration.\n\nDoConnect AI isn't just a Q&A board. It's an intelligent ecosystem that weaves AI into every keystroke to eliminate friction and elevate collective genius.",
        image: "assets/screenshots/home_feed.png"
    },
    {
        tag: "02 // INTELLIGENCE",
        title: "Answers Before You Ask",
        subtitle: "Preemptive Semantic Matching",
        body: "The frustration of asking a question, only to be told 'marked as duplicate,' ends here.\n\nAs you type, our engine invisibly reads your intent in milliseconds and surfaces solved discussions. We don't just organize knowledge; we preempt your roadblocks.",
        image: "assets/screenshots/ai_similar_question_suggestion.png"
    },
    {
        tag: "03 // AUTOMATION",
        title: "Invisible Taxonomy",
        subtitle: "Asynchronous Auto-Tagging",
        body: "Users are bad at categorizing. The backend asynchronously sends the title and description to Gemini, generating the most relevant technology tags.\n\nQuestions are perfectly routed to the right experts without relying on manual user tagging.",
        image: "assets/screenshots/create_question_filled_ai_tag_suggested-3.png"
    },
    {
        tag: "04 // SYNCHRONIZATION",
        title: "Built to Flow",
        subtitle: "Real-Time Ecosystem",
        body: "Instant in-app notifications and global chat keep the community synchronized without polling overhead. The backend pushes events directly to the client's queue.",
        image: "assets/screenshots/global_chat_2.png"
    },
    {
        tag: "05 // ARCHITECTURE",
        title: "Decoupled by Design",
        subtitle: "Stateless vs Stateful",
        body: "The architecture separates concerns to protect the core. High-throughput real-time events run on a dedicated microservice, bridging securely via internal tokens.",
        isArchitecture: true
    },
    {
        tag: "06 // GOVERNANCE",
        title: "A Safe Haven",
        subtitle: "Proactive Community Protection",
        body: "Every interaction is analyzed in milliseconds. Toxicity and spam are intercepted before they ever reach the feed. A focused environment where ideas win, not noise.",
        image: "assets/screenshots/admin_moderation.png"
    },
    {
        tag: "07 // THE BUILDER",
        title: "Engineering with Purpose",
        subtitle: "Mithun Raj M R",
        body: "I build systems that reduce friction.\n\nDoConnect AI combines AI, microservices, real-time communication, and community intelligence into a single platform.\n\nBuilt by Mithun Raj.",
        image: "assets/profile.png"
    }
];

// Image preloader
const imageCache = {};
let loadedImagesCount = 0;
const requiredImages = portfolioData.map(d => d.image).filter(Boolean);

requiredImages.forEach(src => {
    const img = new Image();
    img.onload = () => {
        imageCache[src] = img;
        loadedImagesCount++;
    };
    img.src = src;
});

// Three.js Global Variables
let scene, camera, renderer;
let panels = [];
let airplane;
let dustParticles;

// Scroll & Animation State
let currentScrollZ = 0;
let targetScrollZ = 0;
let activeNavIndex = -1;
let windowScrollY = 0;
let maxScrollHeight = 1;

// Mouse Parallax variables
let mouseX = 0;
let mouseY = 0;
let pointLight;

// Tighter panel spacing to reduce empty scroll space
const panelSpacing = 1000;
const maxScrollDepth = (portfolioData.length - 1) * panelSpacing;

// Wait for fonts and images
waitInterval = setInterval(() => {
    if (loadedImagesCount === requiredImages.length && document.fonts.status === 'loaded') {
        clearInterval(waitInterval);
        initThreeJS();
    }
}, 100);

setTimeout(() => { clearInterval(waitInterval); initThreeJS(); }, 3000);

function initThreeJS() {
    const canvas = document.getElementById('webgl-canvas');

    scene = new THREE.Scene();
    scene.background = null;
    scene.fog = new THREE.FogExp2(0xF4F1EA, 0.0004);

    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 1, 4000);
    camera.position.set(0, 0, 600);

    renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
    // Increase pixel ratio for sharper canvas textures
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2.5));
    renderer.setSize(window.innerWidth, window.innerHeight);

    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.4);
    directionalLight.position.set(200, 500, 300);
    scene.add(directionalLight);

    pointLight = new THREE.PointLight(0xffffff, 0.5, 1500);
    scene.add(pointLight);

    createEnvironment();
    createPanels();
    createAirplane();
    buildNavbar();

    window.addEventListener('resize', onWindowResize);
    window.addEventListener('scroll', onScroll);
    window.addEventListener('mousemove', onMouseMove);

    // Reduce scrollSpacer height to make scrolling faster and end definitively
    scrollSpacer = document.createElement('div');
    scrollSpacer.style.height = `${portfolioData.length * 85}vh`;
    scrollSpacer.style.position = 'absolute';
    scrollSpacer.style.top = '0';
    scrollSpacer.style.width = '1px';
    scrollSpacer.style.zIndex = '-1';
    document.body.appendChild(scrollSpacer);

    calculateScrollBounds();
    onScroll();

    document.getElementById('loader').style.opacity = '0';
    setTimeout(() => document.getElementById('loader').style.display = 'none', 1000);

    animate();
}

function createEnvironment() {
    const particleCount = 800;
    const geometry = new THREE.BufferGeometry();
    const positions = new Float32Array(particleCount * 3);

    for (let i = 0; i < particleCount * 3; i += 3) {
        positions[i] = (Math.random() - 0.5) * 4000;
        positions[i + 1] = (Math.random() - 0.5) * 2000;
        positions[i + 2] = 800 - Math.random() * (maxScrollDepth + 3000);
    }

    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

    const material = new THREE.PointsMaterial({
        color: 0x1E293B,
        size: 4,
        transparent: true,
        opacity: 0.1,
        sizeAttenuation: true
    });

    dustParticles = new THREE.Points(geometry, material);
    scene.add(dustParticles);

    const shapes = [
        new THREE.BoxGeometry(60, 60, 60),
        new THREE.TetrahedronGeometry(50),
        new THREE.OctahedronGeometry(40),
        new THREE.TorusGeometry(40, 10, 8, 20)
    ];

    const wireMaterial = new THREE.LineBasicMaterial({ color: 0x2563EB, transparent: true, opacity: 0.15 });

    for (let i = 0; i < 40; i++) {
        const shapeGeo = shapes[Math.floor(Math.random() * shapes.length)];
        const edges = new THREE.EdgesGeometry(shapeGeo);
        const doodle = new THREE.LineSegments(edges, wireMaterial);

        let x = (Math.random() - 0.5) * 3500;
        let y = (Math.random() - 0.5) * 2500;

        // Push doodles out to the edges so they never intersect the main reading panels
        if (x > -900 && x <= 0) x -= 900;
        if (x > 0 && x < 900) x += 900;
        if (y > -600 && y <= 0) y -= 600;
        if (y > 0 && y < 600) y += 600;

        doodle.position.set(
            x,
            y,
            500 - Math.random() * (maxScrollDepth + 2000)
        );

        doodle.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * Math.PI);

        doodle.userData = {
            rx: (Math.random() - 0.5) * 0.01,
            ry: (Math.random() - 0.5) * 0.01
        };

        scene.add(doodle);
        panels.push({ isDoodle: true, mesh: doodle });
    }
}

function createAirplane() {
    const shape = new THREE.Shape();
    shape.moveTo(0, 0);
    shape.lineTo(20, -5);
    shape.lineTo(0, -40);
    shape.lineTo(-20, -5);
    shape.lineTo(0, 0);

    const extrudeSettings = { depth: 2, bevelEnabled: true, bevelSegments: 2, steps: 1, bevelSize: 0.5, bevelThickness: 0.5 };
    const geometry = new THREE.ExtrudeGeometry(shape, extrudeSettings);

    const material = new THREE.MeshLambertMaterial({ color: 0xffffff, side: THREE.DoubleSide });
    airplane = new THREE.Mesh(geometry, material);

    const edges = new THREE.EdgesGeometry(geometry);
    const lineMaterial = new THREE.LineBasicMaterial({ color: 0x2563EB, linewidth: 2 });
    const line = new THREE.LineSegments(edges, lineMaterial);
    airplane.add(line);

    airplane.rotation.x = Math.PI / 2;
    scene.add(airplane);
}

function createPanels() {
    portfolioData.forEach((data, index) => {
        const texture = createPanelTexture(data, index);

        const geometry = new THREE.BoxGeometry(800, 500, 20);

        const sideMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.8 });
        const frontMat = new THREE.MeshStandardMaterial({
            map: texture,
            transparent: true,
            roughness: 0.9,
            metalness: 0.1
        });
        const materials = [sideMat, sideMat, sideMat, sideMat, frontMat, sideMat];

        const mesh = new THREE.Mesh(geometry, materials);

        const isEven = index % 2 === 0;
        const xOffset = isEven ? 350 : -350;
        const finalX = window.innerWidth < 768 ? 0 : xOffset;

        mesh.position.set(finalX, 0, -index * panelSpacing);

        mesh.rotation.y = isEven ? -0.25 : 0.25;
        mesh.rotation.x = -0.05;
        mesh.rotation.z = (Math.random() - 0.5) * 0.05;

        scene.add(mesh);

        panels.push({
            isPanel: true,
            mesh: mesh,
            targetX: finalX,
            z: mesh.position.z,
            index: index,
            baseRotY: mesh.rotation.y,
            baseRotX: mesh.rotation.x
        });
    });
}

function createPanelTexture(data, index) {
    const canvas = document.createElement('canvas');
    // Ultra High Resolution Canvas (4K+)
    canvas.width = 4096;
    canvas.height = 2560;
    const ctx = canvas.getContext('2d');

    // Scale context so we can still draw in the 1600x1000 logical coordinate space
    ctx.scale(2.56, 2.56);
    ctx.clearRect(0, 0, 1600, 1000);

    // Wonky Paper Background (Kept strictly within 1600x1000 bounds)
    ctx.fillStyle = '#ffffff';
    ctx.shadowColor = 'rgba(0,0,0,0.08)';
    ctx.shadowBlur = 30;
    ctx.shadowOffsetY = 15;

    ctx.beginPath();
    ctx.moveTo(40, 40);
    ctx.lineTo(1560, 30);
    ctx.lineTo(1570, 960);
    ctx.lineTo(30, 970);
    ctx.closePath();
    ctx.fill();

    ctx.shadowColor = 'transparent';

    const isBuilder = index === 6;
    const isArchitecture = data.isArchitecture;

    const textStartX = 100;
    // Constrain text heavily so it never overlaps the right side images/diagrams
    const textMaxWidth = isBuilder ? 1300 : 700;

    ctx.fillStyle = '#2563EB'; // Accent Blue
    ctx.font = '500 32px "Fira Code", monospace';
    ctx.fillText(data.tag, textStartX, 120);

    ctx.fillStyle = '#1E293B';
    ctx.font = '800 100px "Outfit", sans-serif';
    ctx.fillText(data.title, textStartX, 220);

    ctx.fillStyle = '#64748B';
    ctx.font = '700 60px "Caveat", cursive';
    ctx.fillText(data.subtitle, textStartX, 300);

    ctx.strokeStyle = '#2563EB';
    ctx.lineWidth = 4;
    ctx.beginPath();
    ctx.moveTo(textStartX, 330);
    ctx.lineTo(textStartX + 200, 332);
    ctx.lineTo(textStartX + 400, 328);
    ctx.lineTo(textStartX + 550, 335);
    ctx.stroke();

    ctx.fillStyle = '#334155';
    ctx.font = '400 38px "Outfit", sans-serif';
    const lineHeight = 55;
    let cursorY = 420;

    data.body.split('\n').forEach(paragraph => {
        if (paragraph.trim() === '') {
            cursorY += lineHeight * 0.5;
            return;
        }
        const words = paragraph.split(' ');
        let currentLine = '';
        words.forEach((word) => {
            const testLine = currentLine + word + ' ';
            const metrics = ctx.measureText(testLine);
            if (metrics.width > textMaxWidth && currentLine !== '') {
                ctx.fillText(currentLine, textStartX, cursorY);
                currentLine = word + ' ';
                cursorY += lineHeight;
            } else {
                currentLine = testLine;
            }
        });
        ctx.fillText(currentLine, textStartX, cursorY);
        cursorY += lineHeight;
    });

    // Custom Visual Treatments per Card
    // Ensure all images stay within X: 800 to 1550 and Y: 50 to 950
    const img = imageCache[data.image];

    if (index === 0 && img) {
        // Friction: Pinned Polaroid (Larger, moved further right to avoid text overlap)
        ctx.save();
        ctx.translate(1250, 520);
        ctx.rotate(0.05);
        ctx.fillStyle = '#fff';
        ctx.shadowColor = 'rgba(0,0,0,0.15)';
        ctx.shadowBlur = 20;
        ctx.fillRect(-400, -250, 800, 500);
        ctx.shadowColor = 'transparent';
        ctx.drawImage(img, -380, -230, 760, 460);

        ctx.fillStyle = 'rgba(255,255,255,0.6)';
        ctx.fillRect(-50, -270, 100, 30);
        ctx.restore();
    }
    else if (index === 1 && img) {
        // Intelligence: Hand-drawn highlight (Moved down to avoid overlap)
        ctx.drawImage(img, 850, 360, 650, 480);

        ctx.strokeStyle = '#2563EB';
        ctx.lineWidth = 6;
        ctx.beginPath();
        ctx.moveTo(830, 340);
        ctx.lineTo(1520, 350);
        ctx.lineTo(1510, 860);
        ctx.lineTo(840, 850);
        ctx.closePath();
        ctx.stroke();

        ctx.fillStyle = '#2563EB';
        ctx.font = '700 40px "Caveat", cursive';
        ctx.fillText("Intent Match!", 1280, 320);
    }
    else if (index === 2 && img) {
        // Automation: Pasted strip with arrows (Larger and fully visible)
        ctx.drawImage(img, 800, 280, 750, 375);

        ctx.fillStyle = '#2563EB';
        ctx.font = '700 40px "Caveat", cursive';
        ctx.fillText("LLM generated!", 1100, 340); // Moved down and right into empty dark space

        ctx.strokeStyle = '#2563EB';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(1090, 320); // Start left of the text
        ctx.quadraticCurveTo(980, 310, 860, 360); // Curve left towards the first tag
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(860, 360);
        ctx.lineTo(885, 360);
        ctx.lineTo(875, 340);
        ctx.fillStyle = '#2563EB';
        ctx.fill();
    }
    else if (index === 3 && img) {
        // Synchronization: Draw the pre-cropped image exactly into the space
        ctx.save();
        ctx.beginPath();
        ctx.roundRect(850, 80, 650, 800, 10);
        ctx.clip();

        ctx.globalAlpha = 0.95;

        // Draw the image to fit the 650x800 bounding box since it is already cropped correctly
        ctx.drawImage(img, 850, 80, 650, 800);
        ctx.restore();

        // Notebook annotation overlay placed on the RIGHT side, pointing LEFT
        ctx.save();
        ctx.translate(1150, 450);
        ctx.rotate(0.02);

        ctx.fillStyle = '#2563EB';
        ctx.font = '700 32px "Caveat", cursive';
        ctx.fillText("• Live Updates", 0, 0);
        ctx.fillText("• WebSocket Comms", 0, 40);
        ctx.fillText("• No Refresh Required", 0, 80);

        // Hand-drawn arrow pointing left towards the chat bubbles
        ctx.strokeStyle = '#2563EB';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(-20, 20);
        ctx.quadraticCurveTo(-120, 20, -200, -30);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(-200, -30);
        ctx.lineTo(-180, -35);
        ctx.lineTo(-185, -15);
        ctx.fill();
        ctx.restore();
    }
    else if (index === 4) {
        // Architecture: Diagram pushed down and right to avoid text completely
        const cx = 820;
        const cy = 400; // Starts below the title/subtitle

        ctx.strokeStyle = '#2563EB';
        ctx.lineWidth = 3;

        const drawRoughBox = (x, y, w, h, title, sub) => {
            ctx.setLineDash([10, 5, 5, 5]);
            ctx.beginPath();
            ctx.rect(x + (Math.random() * 4 - 2), y + (Math.random() * 4 - 2), w, h);
            ctx.stroke();

            ctx.fillStyle = '#1E293B';
            ctx.font = '600 22px "Fira Code", monospace';
            ctx.fillText(title, x + 20, y + 40);

            if (sub) {
                ctx.fillStyle = '#2563EB';
                ctx.font = '400 26px "Caveat", cursive';
                ctx.fillText(sub, x + 20, y + 75);
            }
        };

        const drawArrow = (x1, y1, x2, y2, label) => {
            ctx.setLineDash([]);
            ctx.beginPath();
            ctx.moveTo(x1, y1);
            ctx.quadraticCurveTo(x1 + (x2 - x1) / 2, y1 + (y2 - y1) / 2 - 10, x2, y2);
            ctx.stroke();

            const angle = Math.atan2(y2 - y1, x2 - x1);
            ctx.beginPath();
            ctx.moveTo(x2, y2);
            ctx.lineTo(x2 - 12 * Math.cos(angle - Math.PI / 6), y2 - 12 * Math.sin(angle - Math.PI / 6));
            ctx.lineTo(x2 - 12 * Math.cos(angle + Math.PI / 6), y2 - 12 * Math.sin(angle + Math.PI / 6));
            ctx.fillStyle = '#2563EB';
            ctx.fill();

            if (label) {
                ctx.fillStyle = '#2563EB';
                ctx.font = '400 24px "Caveat", cursive';
                ctx.fillText(label, (x1 + x2) / 2 + 10, (y1 + y2) / 2 - 10);
            }
        }

        // Layout: React -> Spring -> Gemini (Vertical Left)
        drawRoughBox(cx, cy, 240, 80, "React Frontend", "SPA (Vite)");
        drawRoughBox(cx, cy + 180, 240, 80, "Spring Boot API", "Core Logic");
        drawRoughBox(cx, cy + 360, 240, 80, "Gemini AI", "Intent Engine");

        // Layout: Chat -> Comms (Vertical Right)
        drawRoughBox(cx + 400, cy + 180, 280, 80, "Chat Microservice", "WebSockets");
        drawRoughBox(cx + 400, cy + 360, 280, 80, "Real-Time Comms", "Chat & Notifications");

        // Connections
        drawArrow(cx + 120, cy + 80, cx + 120, cy + 180, "HTTP");
        drawArrow(cx + 120, cy + 260, cx + 120, cy + 360, "REST");

        drawArrow(cx + 240, cy + 210, cx + 400, cy + 210, "Internal Token");
        drawArrow(cx + 400, cy + 230, cx + 240, cy + 230);

        drawArrow(cx + 540, cy + 260, cx + 540, cy + 360, "STOMP Push");
    }
    else if (index === 5 && img) {
        // Governance: Blue Shield/Check
        ctx.drawImage(img, 800, 250, 650, 500);

        ctx.strokeStyle = '#2563EB';
        ctx.lineWidth = 8;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        // Placed off to the right side so text is clearly on white paper
        const sx = 1250;
        const sy = 400;
        ctx.beginPath();
        ctx.moveTo(sx, sy);
        ctx.lineTo(sx + 60, sy + 20);
        ctx.lineTo(sx + 60, sy + 90);
        ctx.quadraticCurveTo(sx + 60, sy + 150, sx, sy + 180);
        ctx.quadraticCurveTo(sx - 60, sy + 150, sx - 60, sy + 90);
        ctx.lineTo(sx - 60, sy + 20);
        ctx.closePath();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(sx - 25, sy + 90);
        ctx.lineTo(sx - 10, sy + 110);
        ctx.lineTo(sx + 30, sy + 60);
        ctx.stroke();

        ctx.fillStyle = '#2563EB';
        ctx.font = '700 36px "Caveat", cursive';
        ctx.fillText("Protected by AI", sx - 80, sy + 220); // Placed safely below the shield
        ctx.restore();
    }
    else if (index === 6 && img) {
        // The Builder
        ctx.save();
        ctx.beginPath();
        ctx.arc(textStartX + 100, cursorY + 100, 80, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip();

        // Zoom in on the profile picture
        ctx.drawImage(img, textStartX - 20, cursorY - 20, 240, 240);
        ctx.restore();

        // Huge hand-drawn arrow guiding the eye to the CTA button, filling the right-side void
        ctx.save();
        ctx.translate(900, 650);
        ctx.rotate(0.1);

        ctx.strokeStyle = '#2563EB';
        ctx.lineWidth = 8;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(250, -80, 500, 150);
        ctx.stroke();

        // Large Arrowhead
        ctx.beginPath();
        ctx.moveTo(480, 100);
        ctx.lineTo(500, 150);
        ctx.lineTo(440, 160);
        ctx.stroke();

        ctx.restore();
    }

    const texture = new THREE.CanvasTexture(canvas);
    texture.generateMipmaps = true;
    texture.minFilter = THREE.LinearMipmapLinearFilter;
    texture.magFilter = THREE.LinearFilter;
    texture.anisotropy = renderer.capabilities.getMaxAnisotropy();
    return texture;
}

function buildNavbar() {
    const navbar = document.getElementById('navbar');
    navbar.innerHTML = '';
    portfolioData.forEach((data, index) => {
        const btn = document.createElement('button');
        btn.className = 'nav-link';
        btn.innerText = data.tag.split('//')[1].trim();

        btn.onclick = (e) => {
            e.preventDefault();
            scrollToPanel(index);
        };

        navbar.appendChild(btn);
    });
}

function calculateScrollBounds() {
    maxScrollHeight = Math.max(1, document.body.scrollHeight - window.innerHeight);
}

function scrollToPanel(index) {
    calculateScrollBounds();
    const percent = (index * panelSpacing) / maxScrollDepth;
    const targetY = percent * maxScrollHeight;
    window.scrollTo({ top: targetY, behavior: 'smooth' });
}

function onMouseMove(event) {
    mouseX = (event.clientX / window.innerWidth) * 2 - 1;
    mouseY = -(event.clientY / window.innerHeight) * 2 + 1;
}

function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
    calculateScrollBounds();
}

function onScroll() {
    windowScrollY = window.scrollY;
    calculateScrollBounds();

    // Exact mapping prevents over-scrolling into empty space
    const scrollPercent = Math.max(0, Math.min(1, windowScrollY / maxScrollHeight));
    targetScrollZ = scrollPercent * maxScrollDepth;

    const hint = document.getElementById('scroll-hint');
    if (hint && windowScrollY > 100) {
        hint.style.opacity = '0';
    } else if (hint) {
        hint.style.opacity = '1';
    }

    const upHint = document.getElementById('scroll-up-hint');
    if (upHint && currentScrollZ > maxScrollDepth - 50) {
        upHint.classList.add('visible');
    } else if (upHint) {
        upHint.classList.remove('visible');
    }

    // Show CTA at the end, and never hide it once it appears
    const cta = document.getElementById('cta-container');
    if (cta && currentScrollZ > maxScrollDepth - 700) {
        cta.classList.remove('hidden');
    }
}

function animate() {
    animationFrameId = requestAnimationFrame(animate);

    currentScrollZ += (targetScrollZ - currentScrollZ) * 0.08;

    if (pointLight) {
        pointLight.position.set(camera.position.x, camera.position.y, camera.position.z - 200);
    }

    let activePanelIndexCalc = Math.abs(Math.round(currentScrollZ / panelSpacing));
    let currentIndex = Math.max(0, Math.min(portfolioData.length - 1, activePanelIndexCalc));

    if (currentIndex !== activeNavIndex) {
        activeNavIndex = currentIndex;
        const links = document.querySelectorAll('.nav-link');
        links.forEach((link, idx) => {
            if (idx === activeNavIndex) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    const activePanel = panels.find(p => p.isPanel && p.index === currentIndex);

    // Reading plane pushed closer (450) to make the active card 70-80% of screen width
    camera.position.z = -currentScrollZ + 460;

    const parallaxX = mouseX * 100;
    const parallaxY = mouseY * 50;

    if (activePanel) {
        // Increase tracking (0.55) to center the card slightly more, preventing clipping while keeping staggering
        const camTargetX = window.innerWidth < 768 ? 0 : (activePanel.targetX * 0.55);
        camera.position.x += ((camTargetX + parallaxX) - camera.position.x) * 0.05;
        camera.position.y += (parallaxY - camera.position.y) * 0.05;

        camera.rotation.y = (camera.position.x * -0.0002) + (mouseX * -0.05);
        camera.rotation.x = mouseY * 0.05;
    }

    const time = Date.now() * 0.001;

    panels.forEach(p => {
        if (p.isPanel) {
            p.mesh.position.y = Math.sin(time + p.index) * 15;
            p.mesh.rotation.y = p.baseRotY + (Math.sin(time * 0.5 + p.index) * 0.02);
        } else if (p.isDoodle) {
            p.mesh.rotation.x += p.mesh.userData.rx;
            p.mesh.rotation.y += p.mesh.userData.ry;
        }
    });

    if (airplane) {
        const planeTargetZ = camera.position.z - 150;
        const bobbing = Math.sin(time) * 10;
        // Smooth out scroll velocity mapping to rotation
        const scrollVelocity = (targetScrollZ - currentScrollZ) * 0.5;

        airplane.position.z = planeTargetZ;
        airplane.position.x += ((mouseX * 150) - airplane.position.x) * 0.1;
        airplane.position.y += (-80 + bobbing + (mouseY * 50) - airplane.position.y) * 0.1;

        const targetRotZ = (airplane.position.x * -0.005) + (Math.sin(time * 0.5) * 0.1) - (mouseX * 0.2);
        const targetRotX = (Math.PI / 2) + (scrollVelocity * 0.002) + (mouseY * 0.2);

        airplane.rotation.z += (targetRotZ - airplane.rotation.z) * 0.1;
        airplane.rotation.x += (targetRotX - airplane.rotation.x) * 0.1;
    }

    if (dustParticles) {
        dustParticles.rotation.y = currentScrollZ * 0.0001;
    }

    renderer.render(scene, camera);
}



        // ==== END INJECTED MAIN.JS ====

        // Override nav links routing logic in the injected JS
        setTimeout(() => {
            const btnSignup = document.getElementById('btn-signup');
            if (btnSignup) {
                btnSignup.onclick = (e) => { e.preventDefault(); navigateToRegister(); };
            }
            const btnEnter = document.getElementById('btn-enter');
            if (btnEnter) {
                btnEnter.onclick = (e) => { e.preventDefault(); navigateToLogin(); };
            }
        }, 3500); // After DOM injected by init

        // Cleanup function
        return () => {
            clearInterval(waitInterval);
            cancelAnimationFrame(animationFrameId);
            window.removeEventListener('resize', onWindowResize);
            window.removeEventListener('scroll', onScroll);
            window.removeEventListener('mousemove', onMouseMove);
            if (scrollSpacer && scrollSpacer.parentNode) {
                scrollSpacer.parentNode.removeChild(scrollSpacer);
            }
            if (scene) {
                scene.traverse((object) => {
                    if (!object.isMesh) return;
                    if (object.geometry) object.geometry.dispose();
                    if (object.material) {
                        if (Array.isArray(object.material)) {
                            object.material.forEach(mat => mat.dispose());
                        } else {
                            object.material.dispose();
                        }
                    }
                });
            }
            if (renderer) {
                renderer.dispose();
                const canvas = document.getElementById('webgl-canvas');
                if (canvas) {
                    const gl = canvas.getContext('webgl') || canvas.getContext('webgl2');
                    if (gl) {
                        const numTextureUnits = gl.getParameter(gl.MAX_TEXTURE_IMAGE_UNITS);
                        for (let unit = 0; unit < numTextureUnits; ++unit) {
                            gl.activeTexture(gl.TEXTURE0 + unit);
                            gl.bindTexture(gl.TEXTURE_2D, null);
                            gl.bindTexture(gl.TEXTURE_CUBE_MAP, null);
                        }
                        gl.bindBuffer(gl.ARRAY_BUFFER, null);
                        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, null);
                        gl.bindRenderbuffer(gl.RENDERBUFFER, null);
                        gl.bindFramebuffer(gl.FRAMEBUFFER, null);
                        const ext = gl.getExtension('WEBGL_lose_context');
                        if (ext) ext.loseContext();
                    }
                }
            }
        };
    }, [isAuthenticated, navigate]);

    if (isAuthenticated) return null;

    return (
        <div className="landing-page">
            <div id="loader">Loading Architecture...</div>

            <canvas id="webgl-canvas"></canvas>

            <div id="ui-layer">
                <div id="header">
                    <div className="brand" onClick={() => window.scrollTo({top:0, behavior:'smooth'})}>
                        DoConnect AI<span>System Architecture</span>
                    </div>
                    <div id="navbar">
                        {/* Links injected by JS */}
                    </div>
                    <div style={{fontWeight: 700, fontFamily: "'Caveat', cursive", fontSize: '1.5rem', color: 'var(--text-main)', pointerEvents: 'auto'}}>
                        Mithun Raj M R
                    </div>
                </div>
                <div id="scroll-hint">
                    <div className="mouse"></div>
                    <p>SCROLL TO EXPLORE</p>
                </div>

                {/* Scroll Up Hint (appears at the end) */}
                <div id="scroll-up-hint">
                    <div className="mouse mouse-up"></div>
                    <p>SCROLL UP TO RETURN</p>
                </div>
                
                {/* Minimal CTA */}
                <div id="cta-container" className="hidden">
                    <button id="btn-enter" className="btn-primary" style={{textDecoration: 'none', display: 'inline-block', border: 'none', background: 'var(--accent)', color: 'var(--bg-color)', padding: '12px 24px', borderRadius: '8px', cursor: 'pointer'}}>Enter DoConnect AI &rarr;</button>
                </div>
            </div>
        </div>
    );
}
