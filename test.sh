#!/bin/bash
# Script para executar testes com output limpo (similar ao Jest/NestJS)
# Uso: ./test.sh

set -euo pipefail

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Limpar tela
clear

echo -e "${BOLD}🧪 Executando testes...${NC}\n"

# Executar testes e capturar saída
OUTPUT=$(./mvnw test 2>&1)

# Extrair informações
echo "$OUTPUT" | grep -E "Running br.com.aguideptbr" | while read -r line; do
    TEST_CLASS=$(echo "$line" | sed 's/.*Running //')
    TEST_NAME=$(echo "$TEST_CLASS" | sed 's/.*\.//' | sed 's/Test$//')
    echo -e "${GRAY}  ○${NC} ${TEST_NAME}"
done

echo ""

# Resultado de cada suite
echo "$OUTPUT" | grep -E "Tests run:" | while read -r line; do
    if echo "$line" | grep -q "Failures: 0, Errors: 0"; then
        # Extrair nome da classe
        CLASS=$(echo "$line" | sed -n 's/.*in \(.*\)/\1/p')
        TEST_NAME=$(echo "$CLASS" | sed 's/.*\.//' | sed 's/Test$//')

        # Extrair números
        TOTAL=$(echo "$line" | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')
        SKIPPED=$(echo "$line" | sed -n 's/.*Skipped: \([0-9]*\).*/\1/p')

        if [ "$SKIPPED" != "0" ]; then
            echo -e "${GREEN}  ✓${NC} ${TEST_NAME} ${GRAY}(${TOTAL} tests, ${SKIPPED} skipped)${NC}"
        else
            echo -e "${GREEN}  ✓${NC} ${TEST_NAME} ${GRAY}(${TOTAL} tests)${NC}"
        fi
    else
        CLASS=$(echo "$line" | sed -n 's/.*in \(.*\)/\1/p')
        TEST_NAME=$(echo "$CLASS" | sed 's/.*\.//' | sed 's/Test$//')

        FAILURES=$(echo "$line" | sed -n 's/.*Failures: \([0-9]*\).*/\1/p')
        ERRORS=$(echo "$line" | sed -n 's/.*Errors: \([0-9]*\).*/\1/p')

        echo -e "${RED}  ✗${NC} ${TEST_NAME} ${RED}(${FAILURES} failures, ${ERRORS} errors)${NC}"
    fi
done

echo ""

# Resumo final
if echo "$OUTPUT" | grep -q "BUILD SUCCESS"; then
    TOTAL_TESTS=$(echo "$OUTPUT" | grep "Tests run:" | tail -1 | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')
    TOTAL_SKIPPED=$(echo "$OUTPUT" | grep "Skipped:" | tail -1 | sed -n 's/.*Skipped: \([0-9]*\).*/\1/p')
    TIME=$(echo "$OUTPUT" | grep "Total time:" | sed -n 's/.*Total time: *\([0-9.]*\) s/\1/p')

    echo -e "${BOLD}${GREEN}✓ Test Suites:${NC} ${BOLD}8 passed${NC}, 8 total"
    echo -e "${BOLD}${GREEN}✓ Tests:${NC}       ${BOLD}${TOTAL_TESTS} passed${NC}${GRAY} (${TOTAL_SKIPPED} skipped)${NC}, ${TOTAL_TESTS} total"
    echo -e "${BOLD}Time:${NC}        ${TIME}s"
    echo ""
    echo -e "${BOLD}${GREEN}✨ Todos os testes passaram!${NC}"
    exit 0
else
    echo -e "${BOLD}${RED}✗ Alguns testes falharam${NC}"
    echo ""
    echo "$OUTPUT" | grep -A 20 "FAILURE"
    exit 1
fi
