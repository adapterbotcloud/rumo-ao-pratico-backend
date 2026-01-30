#!/usr/bin/env node
/**
 * Script para importar questões dos JSONs do Pratico_2025 para o backend
 * 
 * Uso: node import-data.js [API_URL] [EMAIL] [PASSWORD]
 * Padrão: node import-data.js http://localhost:8080/api/v1 admin@pratico.com admin123
 */

const fs = require('fs');
const path = require('path');

const API_URL = process.argv[2] || 'http://localhost:8080/api/v1';
const EMAIL = process.argv[3] || 'admin@pratico.com';
const PASSWORD = process.argv[4] || 'Admin@123';

const DATA_DIR = '/Pratico_2025/dadosJson';

// Mapeamento de arquivo JSON -> assunto (baseado na estrutura do edital)
const TOPIC_MAP = {
  // Os JSONs contêm questões sobre diferentes temas navais
  // Vamos extrair o assunto da bibliografia de cada questão
};

async function request(method, endpoint, body, token) {
  const url = `${API_URL}${endpoint}`;
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  
  const resp = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });
  
  if (!resp.ok) {
    const text = await resp.text();
    throw new Error(`${method} ${endpoint} failed: ${resp.status} - ${text}`);
  }
  
  return resp.json();
}

async function main() {
  console.log('🐐 Rumo ao Prático - Importador de Dados');
  console.log(`API: ${API_URL}`);
  
  // 1. Registrar/login usuário admin
  console.log('\n📝 Registrando usuário admin...');
  try {
    await request('POST', '/auth/register', {
      name: 'Administrador',
      email: EMAIL,
      password: PASSWORD
    });
    console.log('  ✅ Usuário criado');
  } catch (e) {
    console.log('  ℹ️  Usuário já existe, fazendo login...');
  }
  
  const loginResp = await request('POST', '/auth/login', { email: EMAIL, password: PASSWORD });
  const token = loginResp.accessToken;
  console.log('  ✅ Login OK');
  
  // 2. Criar tópicos baseados na bibliografia
  console.log('\n📚 Criando tópicos...');
  const topicCache = {};
  
  const mainTopics = [
    'Arte Naval',
    'Navegação',
    'RIPEAM / COLREG',
    'Legislação',
    'Bridge Team Management',
    'Comunicações',
    'Shiphandling',
    'Rebocadores',
    'PNA - Principles of Naval Architecture',
    'Squat e Interação',
    'Meteorologia e Oceanografia',
    'Manobrabilidade',
    'Geral'
  ];
  
  for (const name of mainTopics) {
    try {
      const topic = await request('POST', '/topics', { name, description: `Questões sobre ${name}` }, token);
      topicCache[name] = topic.id;
      console.log(`  ✅ ${name}`);
    } catch (e) {
      console.log(`  ⚠️  ${name}: ${e.message}`);
    }
  }
  
  // 3. Importar questões
  console.log('\n📋 Importando questões...');
  const files = fs.readdirSync(DATA_DIR).filter(f => f.endsWith('.json')).sort((a, b) => parseInt(a) - parseInt(b));
  
  let totalImported = 0;
  let totalErrors = 0;
  
  for (const file of files) {
    const filePath = path.join(DATA_DIR, file);
    const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    const questions = data.results || [];
    
    if (questions.length === 0) continue;
    
    // Detectar tópico pela bibliografia da primeira questão
    const topicName = detectTopic(questions[0].Bibliografia || '');
    const topicId = topicCache[topicName] || topicCache['Geral'];
    
    try {
      const result = await request('POST', '/questions/import', {
        topicId,
        questions: questions.map(q => ({
          bibliografia: q.Bibliografia || '',
          items: q.Items || '',
          correct: q.correct || '',
          correctAnswer: q.correct_answer || '',
          incorrectAnswers: q.incorrect_answers || [],
          pergunta: q.pergunta || q.question || '',
          questao: q.questao || ''
        }))
      }, token);
      
      totalImported += questions.length;
      process.stdout.write(`  ✅ ${file}: ${questions.length} questões (${topicName})\n`);
    } catch (e) {
      totalErrors += questions.length;
      process.stdout.write(`  ❌ ${file}: ${e.message}\n`);
    }
  }
  
  console.log(`\n🏁 Importação concluída!`);
  console.log(`   ✅ Importadas: ${totalImported}`);
  console.log(`   ❌ Erros: ${totalErrors}`);
  console.log(`   📊 Total: ${totalImported + totalErrors}`);
}

function detectTopic(bib) {
  const lower = bib.toLowerCase();
  if (lower.includes('arte naval')) return 'Arte Naval';
  if (lower.includes('miguens') || lower.includes('navegação')) return 'Navegação';
  if (lower.includes('ripeam') || lower.includes('colreg')) return 'RIPEAM / COLREG';
  if (lower.includes('normam') || lower.includes('lei') || lower.includes('decreto') || lower.includes('lesta')) return 'Legislação';
  if (lower.includes('btm') || lower.includes('bridge team')) return 'Bridge Team Management';
  if (lower.includes('comunicaç') || lower.includes('smcp') || lower.includes('signal') || lower.includes('radioperador')) return 'Comunicações';
  if (lower.includes('shiphandling') || lower.includes('naval shiphandling')) return 'Shiphandling';
  if (lower.includes('rebocador') || lower.includes('tug')) return 'Rebocadores';
  if (lower.includes('pna') || lower.includes('principles of naval')) return 'PNA - Principles of Naval Architecture';
  if (lower.includes('squat') || lower.includes('interaction')) return 'Squat e Interação';
  if (lower.includes('meteoro') || lower.includes('oceano')) return 'Meteorologia e Oceanografia';
  if (lower.includes('manobra') || lower.includes('manoeuvr') || lower.includes('controlabil') || lower.includes('propuls') || lower.includes('resistência')) return 'Manobrabilidade';
  return 'Geral';
}

main().catch(e => {
  console.error('❌ Erro fatal:', e.message);
  process.exit(1);
});
