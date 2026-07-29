#!/bin/bash
# Test script for meta-llama-cpp layer validation

set -e

echo "================================================"
echo "meta-llama-cpp Layer Validation Test"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
PASSED=0
FAILED=0

test_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED++))
}

test_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED++))
}

test_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

# Check if we're in a Yocto build environment
if [ -z "$BUILDDIR" ]; then
    test_fail "Not in a Yocto build environment. Source oe-init-build-env first."
    exit 1
fi

test_pass "Yocto build environment detected: $BUILDDIR"

# Test 1: Check if layer is added
echo ""
echo "Test 1: Checking if meta-llama-cpp layer is added..."
if bitbake-layers show-layers 2>/dev/null | grep -q "meta-llama-cpp"; then
    test_pass "Layer meta-llama-cpp is present"
else
    test_fail "Layer meta-llama-cpp is not added to bblayers.conf"
fi

# Test 2: Validate layer configuration
echo ""
echo "Test 2: Validating layer configuration..."
if [ -f "$(bitbake-layers show-layers | grep meta-llama-cpp | awk '{print $2}')/conf/layer.conf" ]; then
    test_pass "layer.conf exists"
else
    test_fail "layer.conf not found"
fi

# Test 3: Check recipe parsing
echo ""
echo "Test 3: Checking recipe parsing..."
if bitbake-layers show-recipes llama-cpp 2>/dev/null | grep -q "llama-cpp"; then
    test_pass "llama-cpp recipe found and parsed (also provides llama-cpp-server package)"
else
    test_fail "llama-cpp recipe not found or failed to parse"
fi

# Test 4: Check dependencies
echo ""
echo "Test 4: Checking layer dependencies..."
if bitbake-layers show-layers 2>/dev/null | grep -q "openembedded-layer"; then
    test_pass "Dependency openembedded-layer (meta-oe) is present"
else
    test_fail "Dependency openembedded-layer (meta-oe) is missing"
fi

if bitbake-layers show-layers 2>/dev/null | grep -q "meta-python"; then
    test_pass "Dependency meta-python is present"
else
    test_fail "Dependency meta-python is missing"
fi

# Test 5: Dry run build test
echo ""
echo "Test 5: Testing recipe with dry-run parse..."
if bitbake -n llama-cpp 2>&1 | grep -q "ERROR"; then
    test_fail "llama-cpp recipe has parse errors"
else
    test_pass "llama-cpp recipe passed dry-run test"
fi

# Test 6: Check for required files in recipes
echo ""
echo "Test 6: Checking for required recipe files..."
LAYER_PATH=$(bitbake-layers show-layers | grep meta-llama-cpp | awk '{print $2}')

if [ -f "$LAYER_PATH/recipes-llm/llama-cpp/llama-cpp_git.bb" ]; then
    test_pass "llama-cpp recipe file exists"
else
    test_fail "llama-cpp recipe file missing"
fi

if [ -f "$LAYER_PATH/recipes-llm/llama-cpp/files/llama-cpp-server.service" ]; then
    test_pass "systemd service file exists"
else
    test_fail "systemd service file missing"
fi

# Test 7: Check LAYERDEPENDS compatibility
echo ""
echo "Test 7: Checking layer compatibility..."
CURRENT_SERIES=$(bitbake-layers show-layers | grep "^meta " | awk '{print $3}')
test_info "Current series: $CURRENT_SERIES"

# Summary
echo ""
echo "================================================"
echo "Test Summary"
echo "================================================"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "================================================"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Build llama-cpp: bitbake llama-cpp"
    echo "  2. Build server: bitbake llama-cpp-server"
    echo "  3. Build image: bitbake playground-ai-image"
    exit 0
else
    echo -e "${RED}Some tests failed. Please fix the issues above.${NC}"
    exit 1
fi
