// path: fuzz_paths.js

const axios = require('axios');
const fs = require('fs');
const path = require('path');
const http = require('http');
const url = require('url');

// Load wordlist
const wordlistPath = path.join(__dirname, '~/AndroidStudioProjects/Bullet/app/src/main/java/scripts/wordlist.txt');
const wordlist = fs.readFileSync(wordlistPath, 'utf-8').split('\n').filter(Boolean);

// Function to log results
function logResults(results) {
    const logPath = path.join(__dirname, 'fuzzing_results.txt');
    const logData = results.map(result => `Path: ${result.url} - Status: ${result.status}\n`).join('');
    fs.writeFileSync(logPath, logData, 'utf-8');
}

// Fuzzing function
async function fuzzPaths(baseUrl, wordlist) {
    const requests = wordlist.map(async word => {
        const url = `${baseUrl}/${word}`;
        try {
            const response = await axios.get(url);
            return { url, status: response.status };
        } catch (error) {
            if (error.response) {
                return { url, status: error.response.status };
            } else {
                return { url, status: error.message };
            }
        }
    });

    const results = await Promise.all(requests);
    logResults(results);
}

// HTTP server to listen for fuzzing requests
const server = http.createServer((req, res) => {
    const queryObject = url.parse(req.url, true).query;
    const authority = queryObject.authority;

    if (authority) {
        fuzzPaths(`content://${authority}`, wordlist).then(() => {
            res.writeHead(200, {'Content-Type': 'text/plain'});
            res.end('Fuzzing completed. Results saved to fuzzing_results.txt.');
        }).catch(err => {
            res.writeHead(500, {'Content-Type': 'text/plain'});
            res.end(`Error during fuzzing: ${err.message}`);
        });
    } else {
        res.writeHead(400, {'Content-Type': 'text/plain'});
        res.end('Missing authority parameter.');
    }
});

server.listen(3000, () => {
    console.log('Server listening on port 3000');
});
