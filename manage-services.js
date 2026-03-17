const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const LOG_DIR = __dirname;

const services = [
    { name: 'discovery-service', dir: 'infrastrecture/discovery-service', port: 8761, wait: 15 },
    { name: 'config-server', dir: 'infrastrecture/config-server', port: 8888, wait: 20 },
    { name: 'api-gateway', dir: 'infrastrecture/api-gateway', port: 8080, wait: 5 },
    { name: 'keynot-service', dir: 'services/keynot-service', port: 8081, wait: 10 }
];

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function isPortOpen(port) {
    try {
        execSync(`lsof -i :${port}`);
        return true;
    } catch (e) {
        return false;
    }
}

function getPidByPort(port) {
    try {
        const output = execSync(`lsof -t -i :${port} -sTCP:LISTEN`).toString().trim();
        return output.split('\n')[0];
    } catch (e) {
        return null;
    }
}

async function startService(name) {
    const service = services.find(s => s.name === name);
    if (!service) {
        console.log(`\x1b[31mError: Service "${name}" not found.\x1b[0m`);
        return;
    }

    if (isPortOpen(service.port)) {
        console.log(`\x1b[33m${service.name} is already running on port ${service.port}.\x1b[0m`);
        return;
    }

    console.log(`\x1b[32mStarting ${service.name} (Port ${service.port})...\x1b[0m`);
    const logPath = path.join(LOG_DIR, `${service.name}.log`);
    const logStream = fs.createWriteStream(logPath, { flags: 'a' });

    const child = spawn('./mvnw', ['spring-boot:run'], {
        cwd: path.join(__dirname, service.dir),
        detached: true,
        stdio: ['ignore', 'pipe', 'pipe']
    });

    child.stdout.pipe(logStream);
    child.stderr.pipe(logStream);
    child.unref();

    console.log(`Service ${service.name} started. Waiting ${service.wait}s...`);
    await sleep(service.wait * 1000);
}

async function startAll() {
    for (const service of services) {
        await startService(service.name);
    }
    console.log('\x1b[34mAll services initiated.\x1b[0m');
}

function stopService(name) {
    const service = services.find(s => s.name === name);
    if (!service) {
        console.log(`\x1b[31mError: Service "${name}" not found.\x1b[0m`);
        return;
    }

    const pid = getPidByPort(service.port);
    if (pid) {
        console.log(`\x1b[31mStopping ${name} on port ${service.port} (PID ${pid})...\x1b[0m`);
        try {
            execSync(`kill -9 ${pid}`);
            console.log(`\x1b[32m${name} stopped.\x1b[0m`);
        } catch (e) {
            console.log(`\x1b[31mCould not kill ${name}.\x1b[0m`);
        }
    } else {
        console.log(`\x1b[33m${name} is not running.\x1b[0m`);
    }
}

function stopAll() {
    services.slice().reverse().forEach(s => stopService(s.name));
}

function status() {
    console.log('\x1b[34m╔════════════════════════════════════════════════════════════╗\x1b[0m');
    console.log('\x1b[34m║                 Conferix Service Status                    ║\x1b[0m');
    console.log('\x1b[34m╠══════════════════════╦════════════╦═══════════╦════════════╣\x1b[0m');
    console.log('\x1b[34m║ Service Name         ║ Port       ║ Status    ║ PID        ║\x1b[0m');
    console.log('\x1b[34m╠══════════════════════╬════════════╬═══════════╬════════════╣\x1b[0m');

    services.forEach(s => {
        const open = isPortOpen(s.port);
        const pid = open ? getPidByPort(s.port) : '----';
        const colorStat = open ? '\x1b[32mRUNNING\x1b[0m' : '\x1b[31mSTOPPED\x1b[0m';
        const statusStr = open ? 'RUNNING' : 'STOPPED';

        const namePad = s.name.padEnd(20);
        const portPad = s.port.toString().padEnd(10);
        const statPad = statusStr.padEnd(9);
        const pidPad = pid.toString().padEnd(10);

        console.log(`\x1b[34m║\x1b[0m ${namePad} \x1b[34m║\x1b[0m ${portPad} \x1b[34m║\x1b[0m ${colorStat}${' '.repeat(9 - statusStr.length)} \x1b[34m║\x1b[0m ${pidPad} \x1b[34m║\x1b[0m`);
    });
    console.log('\x1b[34m╚══════════════════════╩════════════╩═══════════╩════════════╝\x1b[0m');
}

const command = process.argv[2];
const target = process.argv[3];

(async () => {
    switch (command) {
        case 'start':
            if (target) await startService(target);
            else await startAll();
            break;
        case 'stop':
            if (target) stopService(target);
            else stopAll();
            break;
        case 'status':
            status();
            break;
        default:
            console.log('Usage: node manage-services.js [start|stop|status] [service-name]');
            console.log('Services: discovery-service, config-server, api-gateway');
            break;
    }
})();
