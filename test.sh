#!/bin/bash
# Script para executar testes com output limpo (similar ao Jest/NestJS)
# Uso: ./test.sh

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
GRAY='\033[0;90m'
NC='\033[0m'
BOLD='\033[1m'

clear

echo -e "${BOLD}🧪 Executando testes...${NC}\n"

# Arquivo temporário
TMPFILE="/tmp/maven-test-$$.txt"

# Executar testes
./mvnw test -B > "$TMPFILE" 2>&1

# Processar classes testadas
grep "Running br.com.aguideptbr" "$TMPFILE" | while read -r line; do
    CLASS=$(echo "$line" | sed 's/.*Running //' | sed 's/.*\.//' | sed 's/Test$//')
    echo -e "${GRAY}  ○${NC} ${CLASS}"
done

echo ""

# Processar resultados
grep "Tests run:" "$TMPFILE" | while read -r line; do
    CLASS=$(echo "$line" | sed -n 's/.*in \(.*\)/\1/p' | sed 's/.*\.//' | sed 's/Test$//')
    TOTAL=$(echo "$line" | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')
    SKIPPED=$(echo "$line" | sed -n 's/.*Skipped: \([0-9]*\).*/\1/p')

    if echo "$line" | grep -q "Failures: 0, Errors: 0"; then
        if [ -n "$SKIPPED" ] && [ "$SKIPPED" != "0" ]; then
            echo -e "${GREEN}  ✓${NC} ${CLASS} ${GRAY}(${TOTAL} tests, ${SKIPPED} skipped)${NC}"
        else
            echo -e "${GREEN}  ✓${NC} ${CLASS} ${GRAY}(${TOTAL} tests)${NC}"
        fi
    else
        FAIL=$(echo "$line" | sed -n 's/.*Failures: \([0-9]*\).*/\1/p')
        ERR=$(echo "$line" | sed -n 's/.*Errors: \([0-9]*\).*/\1/p')
        echo -e "${RED}  ✗${NC} ${CLASS} ${RED}(${FAIL} failures, ${ERR} errors)${NC}"
    fi
done

echo ""

# Resumo
if grep -q "BUILD SUCCESS" "$TMPFILE"; then
    TOTAL=$(grep "Tests run:" "$TMPFILE" | tail -1 | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')
    SKIP=$(grep "Tests run:" "$TMPFILE" | tail -1 | sed -n 's/.*Skipped: \([0-9]*\).*/\1/p')
    SUITES=$(grep -c "Tests run:" "$TMPFILE")
    TIME=$(grep "Total time:" "$TMPFILE" | sed -n 's/.*Total time: *\(.*\)/\1/p')

    echo -e "${BOLD}${GREEN}✓ Test Suites:${NC} ${BOLD}${SUITES} passed${NC}, ${SUITES} total"
    echo -e "${BOLD}${GREEN}✓ Tests:${NC}       ${BOLD}${TOTAL} passed${NC}${GRAY} (${SKIP} skipped)${NC}, ${TOTAL} total"
    echo -e "${BOLD}Time:${NC}        ${TIME}"
    echo ""
    echo -e "${BOLD}${GREEN}✨ Todos os testes passaram!${NC}"
    rm -f "$TMPFILE"
    exit 0
else
    echo -e "${BOLD}${RED}✗ Alguns testes falharam${NC}\n"
    grep -A 20 "FAILURE" "$TMPFILE"
    rm -f "$TMPFILE"
    exit 1
fi
