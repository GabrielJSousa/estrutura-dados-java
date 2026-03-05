package aed.trees;
// ====================================================================================
// Implementação de uma árvore de pesquisa binária autobalanceada através do peso
// ====================================================================================

import org.w3c.dom.Node;

import java.util.*;

// =================================
// Classe que trata dos nodes
// =================================

class UAlgTreeNode<Key extends Comparable<Key>,Value> implements IUAlgTreeNode<Key, Value>
{
    Key chave;
    Value valor;
    UAlgTreeNode<Key,Value> esq;
    UAlgTreeNode<Key,Value> dir;
    int size;
    int peso;

    public UAlgTreeNode(Key chave, Value valor){
        this.chave = chave;
        this.valor = valor;
        this.esq = null;
        this.dir = null;
        this.peso = 2;
        this.size = 1;
    }

    // ===========================
    // Getters da interface
    // ===========================
    @Override
    public IUAlgTreeNode<Key, Value> getLeft() {
        return esq;
    }

    @Override
    public IUAlgTreeNode<Key, Value> getRight() {
        return dir;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getWeight() {
        return peso;
    }

    @Override
    public Key getKey() {
        return chave;
    }

    @Override
    public Value getValue() {
        return valor;
    }
}

// ============================
// Classe que trata da árvore
// ============================

public class UAlgTree<Key extends Comparable<Key>,Value> {

    private UAlgTreeNode<Key, Value> raiz;

    // ==========================================
    // Métodos auxiliares para o balanceamento
    // ==========================================

    private int W(UAlgTreeNode<Key,Value> n){
        if (n == null) return 0;

        else return n.peso;
    }

    private void atualizar(UAlgTreeNode<Key,Value> n){
        if (n == null) return;
        n.size = 1 + size(n.esq) + size(n.dir);
        n.peso = W(n.esq) +  W(n.dir) + 1;
    }


    // ==========================
    // Rotações
    // ==========================
    private UAlgTreeNode<Key,Value> rotateLeft(UAlgTreeNode<Key,Value>  Leftnode){
        UAlgTreeNode<Key,Value>  Rightnode = Leftnode.dir;
        Leftnode.dir = Rightnode.esq;
        Rightnode.esq = Leftnode;

        atualizar(Leftnode);
        atualizar(Rightnode);

        return Rightnode;
    }

    private UAlgTreeNode<Key, Value> rotateRight(UAlgTreeNode<Key, Value> Rightnode){
        UAlgTreeNode<Key,Value>  Leftnode = Rightnode.esq;
        Rightnode.esq = Leftnode.dir;
        Leftnode.dir = Rightnode;

        atualizar(Rightnode);
        atualizar(Leftnode);

        return Leftnode;
    }

    // Balanceamento do nó (LL, RR, LR, RL)
    private UAlgTreeNode<Key, Value> balanceamento (UAlgTreeNode<Key,Value> n){
        if (n == null) return null;

        // Balanceamento à esquerda
        if(W(n.esq) > 2.5 * W(n.dir)){

            UAlgTreeNode<Key,Value>  x = n.esq;

            // Caso LR rotação dupla
            if (x != null && W(x.dir) > 1.5 * W(x.esq)){
                n.esq = rotateLeft(x);
            }

            n = rotateRight(n);
        }

        // Balanceamento à direita
        else if (W(n.dir) > 2.5 * W(n.esq)){
            UAlgTreeNode<Key,Value>  x = n.dir;

            // Caso RL rotação dupla
            if (n.dir != null && W(x.esq) > 1.5 * W(x.dir)){
                n.dir = rotateRight(x);
            }

            n = rotateLeft(n);
        }

        atualizar(n);
        return n;
    }

    //============================
    // Construtor
    // ==========================
    public UAlgTree() {this.raiz = null;}

    // ===============================================
    // Métodos de acesso e pesquisa
    // ================================================
    public IUAlgTreeNode<Key,Value> getRoot() {return this.raiz;}


    public int size(){
        return size(this.raiz);
    }
    private int size(UAlgTreeNode<Key,Value> n)
    {
        // A árvore está vazia
        return (n == null) ? 0 : n.getSize();
    }


    private boolean isRotationSafeRight(UAlgTreeNode<Key, Value> p) {
        if (p == null || p.dir == null) return false;
        UAlgTreeNode<Key, Value> n = p.dir;
        UAlgTreeNode<Key, Value> e = n.esq;
        UAlgTreeNode<Key, Value> d = n.dir;


        return W(p) <= 3.5 * W(d) && W(p) <= 3.5 * W(e) + W(d);
    }

    private boolean isRotationSafeLeft(UAlgTreeNode<Key, Value> p) {
        if (p == null || p.esq == null) return false;
        UAlgTreeNode<Key, Value> n = p.esq;
        UAlgTreeNode<Key, Value> e = n.esq;
        UAlgTreeNode<Key, Value> d = n.dir;


        return W(p) <= 3.5 * W(e) && W(p) <= 3.5 * W(d) + W(e);
    }

    private Value valAux;
    // Variável para saber se já fizemos a rotação "obrigatória" nesta pesquisa
    private boolean jaRodou;

    public Value get(Key k) {
        if (k == null) return null;

        // Reset às variáveis de controlo
        valAux = null;
        jaRodou = false;

        this.raiz = get(this.raiz, k);

        return valAux;
    }

    private UAlgTreeNode<Key, Value> get(UAlgTreeNode<Key, Value> n, Key k) {
        if (n == null) return null;

        int cmp = k.compareTo(n.chave);

        if (cmp < 0) {
            // --- DESCER À ESQUERDA ---
            n.esq = get(n.esq, k);

            // --- VOLTAR DA RECURSÃO (BACKTRACKING) ---
            // Se encontrámos o valor (valAux != null) E ainda não rodámos ninguém...
            if (valAux != null && !jaRodou) {

                // O nó que queremos subir é o n.left.
                // Verificamos se n é a Raiz Absoluta OU se é seguro rodar.
                if (n == this.raiz || isRotationSafeLeft(n)) {
                    n = rotateRight(n);
                    jaRodou = true; // Marcamos como feito para não rodar mais acima
                }
                // Se não for seguro, não fazemos nada aqui.
                // A recursão continua para cima e tentaremos rodar o Pai deste 'n'.
            }
        }
        else if (cmp > 0) {
            // --- DESCER À DIREITA ---
            n.dir = get(n.dir, k);

            // --- VOLTAR DA RECURSÃO ---
            if (valAux != null && !jaRodou) {

                // O nó que queremos subir é o n.right.
                if (n == this.raiz || isRotationSafeRight(n)) {
                    n = rotateLeft(n);
                    jaRodou = true;
                }
            }
        }
        else {
            // --- ENCONTRÁMOS O NÓ ---
            valAux = n.valor;
            // Não rodamos aqui. A rotação acontece no Pai (quando a função retornar).
            return n;
        }
        atualizar(n);
        return n;
    }

    public boolean contains(Key k)
    {
        if (k == null) return false;

        return contains(k, this.raiz);
    }
    private boolean contains(Key k, UAlgTreeNode<Key,Value> n){
        if (n == null) return false;

        int cmp = k.compareTo(n.chave);

        if (cmp < 0) return contains(k, n.esq);
        else if (cmp > 0) return contains(k, n.dir);
        else return true;
    }

    // ==================================
    // Inserção
    // ==================================
    public void put(Key k, Value v)
    {
        if (k == null) throw new IllegalArgumentException("A chave não pode ser nula!");
        if (v == null) {
            delete(k);
            return;
        }
        this.raiz = put(this.raiz, k, v);
    }
    private UAlgTreeNode<Key, Value> put(UAlgTreeNode<Key,Value> n, Key k, Value v){
        // O construtor já tem size = 1 e peso = 2 (sem filhos)
        if (n == null) return new UAlgTreeNode<>(k, v);

        int cmp = k.compareTo(n.chave);

        if (cmp < 0) n.esq = put(n.esq, k, v);
        else if (cmp > 0) n.dir = put(n.dir, k, v);
        else {n.valor = v;}

        atualizar(n);
        // A função modifica também a variável local, ou seja, também altera o que está a apontar para a raiz

        return balanceamento(n);
    }

    // ==================================
    // Remoção
    // ==================================
    public void delete(Key k)
    {
        if (k == null) throw new IllegalArgumentException("A chave não pode ser nula!");
        this.raiz = delete(this.raiz, k);
    }
    private UAlgTreeNode<Key, Value> minimo(UAlgTreeNode<Key,Value> n){
        if (n.esq == null) return n;
        return minimo(n.esq);
    }
    private UAlgTreeNode<Key, Value> delete(UAlgTreeNode<Key,Value> n, Key k){
        if (n == null) return null;

        int cmp = k.compareTo(n.chave);

        if (cmp < 0) n.esq = delete(n.esq, k);
        else if (cmp > 0) n.dir = delete(n.dir, k);

        else{
            //=============================
            // Nó sem filhos ou com 1 filho
            // ============================
            if (n.esq == null) return n.dir; // Não tem filho esquerdo então aponta-se para o da direita
            if (n.dir == null) return n.esq; // o mesmo mas ao contrário

            // ==================
            // Nó com 2 filhos
            // ==================
            UAlgTreeNode<Key,Value>  n2 = minimo(n.dir);

            n.chave = n2.chave;
            n.valor = n2.valor;

            n.dir = delete(n.dir, n2.chave);
        }

        atualizar(n);

        return balanceamento(n);
    }


    // ================================
    // Iteradores
    // ================================
    public Iterable<Key> keys()
    {
        ArrayList<Key> listachaves = new ArrayList<>();

        keys (this.raiz, listachaves);

        return listachaves;
    }
    private void keys(UAlgTreeNode<Key, Value> node, List<Key> listachaves){

        if (node == null) return;

        // Garantimos que as chaves menores são processadas primeiro
        keys(node.esq, listachaves);

        // Adiciona a chave do nó atual à lista
        listachaves.add(node.chave);

        // Garante que as chaves maiores são processadas por último
        keys(node.dir, listachaves);

        //Explicação:
        //        10 (Pai)
        //       /  \
        //      5    15 (Irmão do 5)
        //     / \
        //    2   7 (Filho Direito do 5)
        //Nós queremos começar pelo menor elemento, então: começamos em 10, chamamos keys raiz.esq e entramos no 5,
        //aqui 5 vai chamar keys raiz.esq de novo e chegamos a 2, chamamos keys raiz esq de novo, mas esta retorna null,
        //então fazemos add do 2 para a lista. Raiz.dir de 2 também será null então nada acontece
        //voltamos agora à chamada que ficou em espera, o 5, fazemos add do 5 para a lista e a seguir fazemos keys raiz.dir,
        //o que vai fazer add do 7 para a lista. Já temos [2, 5, 7]. Voltamos de novo à chamada que ficou em espera, o 10,
        //fazemos add do 10 e chamamos keys para raiz.dir, ou seja, o 15. Este como não tem filhos, as chamadas vão retornar null
        //e vamos apenas fazer add do 15. No fim temos [2, 5, 7, 10, 15].
    }

    public Iterable<Value> values()
    {
        List<Value> listavalores = new ArrayList<>();

        Values (this.raiz, listavalores);

        return listavalores;
    }
    private void Values(UAlgTreeNode<Key, Value> node, List<Value> listavalores){

        if (node == null) return;

        // Garantimos que as chaves menores são processadas primeiro
        Values(node.esq, listavalores);

        // Adiciona a chave do nó atual à lista
        listavalores.add(node.valor);

        // Garante que as chaves maiores são processadas por último
        Values(node.dir, listavalores);
    }

    // ====================================
    // Cópia
    // ====================================
    public UAlgTree<Key,Value> shallowCopy()
    {
        UAlgTree<Key, Value> copia = new UAlgTree<>();
        copia.raiz = shallowCopy(this.raiz);
        return copia;
    }
    private UAlgTreeNode<Key, Value> shallowCopy(UAlgTreeNode<Key, Value> n){
        if (n == null) return null;

        UAlgTreeNode<Key,Value> novo_no = new UAlgTreeNode<>(n.chave, n.valor);
        novo_no.size = n.size;
        novo_no.peso = n.peso;

        novo_no.esq = shallowCopy(n.esq);
        novo_no.dir = shallowCopy(n.dir);

        return novo_no;
    }

    // ===============================================
    // Métodos para Análise de Profundidade (Parte C)
    // ===============================================

    public int maxDepth() {
        return maxDepth(this.raiz);
    }

    private int maxDepth(UAlgTreeNode<Key, Value> n) {
        if (n == null) return 0;
        return 1 + Math.max(maxDepth(n.esq), maxDepth(n.dir));
    }

    public int minDepth() {
        return minDepth(this.raiz);
    }

    private int minDepth(UAlgTreeNode<Key, Value> n) {
        if (n == null) return 0;
        // Se é folha
        if (n.esq == null && n.dir == null) return 1;
        // Se tem apenas um filho, temos de seguir por esse lado
        if (n.esq == null) return 1 + minDepth(n.dir);
        if (n.dir == null) return 1 + minDepth(n.esq);

        // Se tem dois filhos, o caminho mais curto
        return 1 + Math.min(minDepth(n.esq), minDepth(n.dir));
    }

    // =============================
    // Main com testes
    // =============================
    public static void main(String[] args) {
        // 1. Instanciar a Árvore
        UAlgTree<Integer, String> arvore = new UAlgTree<>();

        System.out.println("\n==============================");
        System.out.println(" TESTE DE BALANCEAMENTO (LL)");
        System.out.println("==============================");

        UAlgTree<Integer, String> tLL = new UAlgTree<>();
        tLL.put(30, "A");
        tLL.put(20, "B");
        System.out.println("\n--- DEBUG: Inserção que aciona LL (Chave 10) ---");
        tLL.put(10, "C");  // Espera rotação direita → raiz = 20

        IUAlgTreeNode<Integer, String> rLL = tLL.getRoot();
        System.out.println("Raiz esperada = 20 → " + rLL.getKey());
        System.out.println("Esq esperada = 10 → " + rLL.getLeft().getKey());
        System.out.println("Dir esperada = 30 → " + rLL.getRight().getKey());

        System.out.println("\n==============================");
        System.out.println(" TESTE DE BALANCEAMENTO (RR)");
        System.out.println("==============================");

        UAlgTree<Integer, String> tRR = new UAlgTree<>();
        tRR.put(10, "A");
        tRR.put(20, "B");
        tRR.put(30, "C");  // Espera rotação esquerda → raiz = 20

        IUAlgTreeNode<Integer, String> rRR = tRR.getRoot();
        System.out.println("Raiz esperada = 20 → " + rRR.getKey());
        System.out.println("Esq esperada = 10 → " + rRR.getLeft().getKey());
        System.out.println("Dir esperada = 30 → " + rRR.getRight().getKey());

        System.out.println("\n==============================");
        System.out.println(" TESTE DE BALANCEAMENTO (LR)");
        System.out.println("==============================");

        UAlgTree<Integer, String> tLR = new UAlgTree<>();
        tLR.put(30, "A");
        tLR.put(10, "B");
        tLR.put(20, "C");  // Espera rotação dupla → raiz = 20

        IUAlgTreeNode<Integer, String> rLR = tLR.getRoot();
        System.out.println("Raiz esperada = 20 → " + rLR.getKey());
        System.out.println("Esq esperada = 10 → " + rLR.getLeft().getKey());
        System.out.println("Dir esperada = 30 → " + rLR.getRight().getKey());

        System.out.println("\n==============================");
        System.out.println(" TESTE DE BALANCEAMENTO (RL)");
        System.out.println("==============================");

        UAlgTree<Integer, String> tRL = new UAlgTree<>();
        tRL.put(10, "A");
        tRL.put(30, "B");
        tRL.put(20, "C");  // Espera rotação dupla → raiz = 20

        IUAlgTreeNode<Integer, String> rRL = tRL.getRoot();
        System.out.println("Raiz esperada = 20 → " + rRL.getKey());
        System.out.println("Esq esperada = 10 → " + rRL.getLeft().getKey());
        System.out.println("Dir esperada = 30 → " + rRL.getRight().getKey());

        System.out.println("\n==============================");
        System.out.println(" TESTES ADICIONAIS DE BALANCEAMENTO");
        System.out.println("==============================");

        // Teste: Inserção em ordem crescente → múltiplas rotações RR
        UAlgTree<Integer, String> tCresc = new UAlgTree<>();
        for (int i = 1; i <= 7; i++) tCresc.put(i, "V" + i);
        System.out.println("Raiz após inserções crescentes: " + tCresc.getRoot().getKey());
        System.out.println("Tamanho da árvore: " + tCresc.size());

        // Teste: Inserção em ordem decrescente → múltiplas rotações LL
        UAlgTree<Integer, String> tDecresc = new UAlgTree<>();
        for (int i = 7; i >= 1; i--) tDecresc.put(i, "V" + i);
        System.out.println("Raiz após inserções decrescentes: " + tDecresc.getRoot().getKey());
        System.out.println("Tamanho da árvore: " + tDecresc.size());

        // Teste: Rotação LR
        UAlgTree<Integer, String> tLR2 = new UAlgTree<>();
        tLR2.put(30, "A");
        tLR2.put(10, "B");
        tLR2.put(20, "C");
        System.out.println("Raiz após inserção LR: " + tLR2.getRoot().getKey());
        System.out.println("Esq: " + tLR2.getRoot().getLeft().getKey());
        System.out.println("Dir: " + tLR2.getRoot().getRight().getKey());

        // Teste: Rotação RL
        UAlgTree<Integer, String> tRL2 = new UAlgTree<>();
        tRL2.put(10, "A");
        tRL2.put(30, "B");
        tRL2.put(20, "C");
        System.out.println("Raiz após inserção RL: " + tRL2.getRoot().getKey());
        System.out.println("Esq: " + tRL2.getRoot().getLeft().getKey());
        System.out.println("Dir: " + tRL2.getRoot().getRight().getKey());

        // Teste: Inserção misturada (LL + RR + LR + RL)
        UAlgTree<Integer, String> tMix = new UAlgTree<>();
        int[] keys = {50, 20, 70, 10, 30, 60, 80, 25, 35, 65};
        for (int k : keys) tMix.put(k, "V" + k);
        System.out.println("Raiz após inserção mista: " + tMix.getRoot().getKey());
        System.out.println("Peso da raiz: " + tMix.getRoot().getWeight());
        System.out.println("Tamanho da árvore: " + tMix.size());

        // Imprime keys em ordem para verificar estrutura
        System.out.print("Chaves in-order: ");
        for (Integer k : tMix.keys()) System.out.print(k + " ");
        System.out.println();


        System.out.println("\n==============================");
        System.out.println(" TESTES CONCLUÍDOS");
        System.out.println("==============================");

        System.out.println("--- Teste de Inserção (put) e Tamanho (size) ---");

        // 2. Inserir alguns pares (Chave, Valor)
        arvore.put(50, "Cinquenta");
        arvore.put(30, "Trinta");
        arvore.put(70, "Setenta");
        System.out.println("Tamanho após 3 inserções: " + arvore.size()); // Esperado: 3

        arvore.put(20, "Vinte");
        arvore.put(40, "Quarenta");
        arvore.put(60, "Sessenta");
        arvore.put(80, "Oitenta");
        System.out.println("Tamanho após 7 inserções: " + arvore.size()); // Esperado: 7

        // 3. Teste de atualização de valor (put com chave existente)
        arvore.put(50, "Cinquenta_Novo");
        System.out.println("Valor da chave 50 (após atualização): " + arvore.get(50)); // Esperado: Cinquenta_Novo
        System.out.println("Tamanho após atualização: " + arvore.size()); // Esperado: 7

        System.out.println("\n--- Teste de Acesso (get) e Contém (contains) ---");

        System.out.println("Valor da chave 30: " + arvore.get(30)); // Esperado: Trinta
        System.out.println("Valor da chave 99 (não existe): " + arvore.get(99)); // Esperado: null
        System.out.println("Contém chave 40? " + arvore.contains(40)); // Esperado: true
        System.out.println("Contém chave 10? " + arvore.contains(10)); // Esperado: false

        System.out.println("\n--- Teste de Pesos (W) da Raiz ---");
        System.out.println("Peso total: " + arvore.getRoot().getWeight());

        System.out.println("\n==============================");
        System.out.println(" TESTES DE DELETE()");
        System.out.println("==============================");

        UAlgTree<Integer, String> tDel = new UAlgTree<>();
        tDel.put(50, "A");
        tDel.put(30, "B");
        tDel.put(70, "C");
        tDel.put(20, "D");
        tDel.put(40, "E");
        tDel.put(60, "F");
        tDel.put(80, "G");

        System.out.println("\n--- 1) Remoção de folha ---");
        tDel.delete(20);
        System.out.println("Contains 20 (false): " + tDel.contains(20));

        System.out.println("\n--- 2) Remoção com 1 filho ---");
        tDel.delete(30); // 30 tinha apenas 40
        System.out.println("Contains 30 (false): " + tDel.contains(30));
        System.out.println("Contains 40 (true): " + tDel.contains(40));

        System.out.println("\n--- 3) Remoção com 2 filhos ---");
        tDel.delete(50); // raiz com 2 filhos → substituída pelo min da direita (60)
        System.out.println("Contains 50 (false): " + tDel.contains(50));
        System.out.println("Nova raiz esperada (60): " + tDel.getRoot().getKey());

        System.out.println("\n--- 4) Remoção que obriga rotação LL ---");
        UAlgTree<Integer, String> tLLdel = new UAlgTree<>();
        tLLdel.put(30, "A");
        tLLdel.put(20, "B");
        tLLdel.put(40, "C");
        tLLdel.put(10, "D");
        tLLdel.delete(40); // desbalanceia à esquerda
        System.out.println("Nova raiz esperada = 20 → " + tLLdel.getRoot().getKey());

        System.out.println("\n--- 5) Remoção que obriga rotação RR ---");
        UAlgTree<Integer, String> tRRdel = new UAlgTree<>();
        tRRdel.put(10, "A");
        tRRdel.put(20, "B");
        tRRdel.put(30, "C");
        tRRdel.put(25, "D");
        tRRdel.delete(10); // desbalanceia à direita
        System.out.println("Nova raiz esperada = 25 → " + tRRdel.getRoot().getKey());

        System.out.println("\n--- 6) Remoção que obriga rotação LR ---");
        UAlgTree<Integer, String> tLRdel = new UAlgTree<>();
        tLRdel.put(40, "A");
        tLRdel.put(10, "B");
        tLRdel.put(30, "C");
        tLRdel.put(20, "D");
        tLRdel.delete(40); // desbalanceia à esquerda
        System.out.println("Nova raiz esperada = 20 → " + tLRdel.getRoot().getKey());

        System.out.println("\n--- 7) Remoção que obriga rotação RL ---");
        UAlgTree<Integer, String> tRLdel = new UAlgTree<>();
        tRLdel.put(10, "A");
        tRLdel.put(50, "B");
        tRLdel.put(30, "C");
        tRLdel.put(40, "D");
        tRLdel.delete(10); // desbalanceia à direita
        System.out.println("Nova raiz esperada = 40 → " + tRLdel.getRoot().getKey());




        System.out.println(">>> INÍCIO DOS TESTES GERAIS DA PARTE C <<<");
        Random rnd = new Random();
        int N_BALANCE = 100_000;

        // =====================================================================
        // 1. ANÁLISE DE BALANCEAMENTO (Aleatório - Múltiplas Execuções)
        // =====================================================================
        System.out.println("\n--- 1. Estabilidade do Balanceamento (Aleatório: 5 Execuções) ---");

        for (int i = 1; i <= 5; i++) {
            UAlgTree<Integer, Integer> tree = new UAlgTree<>();
            for (int k = 0; k < N_BALANCE; k++) {
                tree.put(rnd.nextInt(N_BALANCE * 10), k);
            }
            double maxD = tree.maxDepth();
            double minD = tree.minDepth();
            double ratio = (minD == 0) ? 0 : maxD / minD;
            System.out.printf("Execução #%d: Max=%.0f | Min=%.0f | Rácio=%.4f\n", i, maxD, minD, ratio);
        }

        // =====================================================================
        // 2. ANÁLISE DE BALANCEAMENTO (Ordenado - Pior Caso)
        // =====================================================================
        System.out.println("\n--- 2. Teste do Pior Caso (Inserção Ordenada Crescente) ---");

        UAlgTree<Integer, Integer> treeOrdered = new UAlgTree<>();
        for (int i = 0; i < N_BALANCE; i++) {
            treeOrdered.put(i, i);
        }
        double maxD = treeOrdered.maxDepth();
        double minD = treeOrdered.minDepth();
        double ratioOrdered = (minD == 0) ? 0 : maxD / minD;
        System.out.printf("[Ordenado] N=%d | Max=%.0f | Min=%.0f | Rácio=%.4f\n", N_BALANCE, maxD, minD, ratioOrdered);


        // =====================================================================
        // 3. EFICIÊNCIA DE PESQUISA (Uniforme vs Pareto)
        // =====================================================================
        System.out.println("\n--- 3. Eficiência de Pesquisa (Uniforme vs Pareto) ---");

        int[] sizes = {10000, 20000, 40000, 80000};
        int NUM_SEARCHES = 200_000;

        System.out.printf("%-10s | %-15s | %-15s | %-10s\n", "N", "Tempo Uniforme", "Tempo Pareto", "Melhoria");
        System.out.println("---------------------------------------------------------------");

        for (int N : sizes) {
            UAlgTree<Integer, Integer> tree = new UAlgTree<>();
            ArrayList<Integer> listaAuxiliar = new ArrayList<>(N);

            for (int i = 0; i < N; i++) {
                tree.put(i, i);
                listaAuxiliar.add(i);
            }

            // --- TESTE UNIFORME ---
            long startUniform = System.nanoTime();
            for (int i = 0; i < NUM_SEARCHES; i++) {
                int index = rnd.nextInt(N);
                tree.get(listaAuxiliar.get(index));
            }
            long timeUniform = System.nanoTime() - startUniform;

            // --- TESTE PARETO (Recriar árvore para ser justo) ---
            tree = new UAlgTree<>();
            for (int k : listaAuxiliar) tree.put(k, k);

            int hotCount = (int) (N * 0.2);
            long startPareto = System.nanoTime();
            for (int i = 0; i < NUM_SEARCHES; i++) {
                double p = rnd.nextDouble();
                int index;
                if (p < 0.8) index = rnd.nextInt(hotCount); // 80% das vezes nos primeiros 20%
                else index = hotCount + rnd.nextInt(N - hotCount);

                tree.get(listaAuxiliar.get(index));
            }
            long timePareto = System.nanoTime() - startPareto;

            // Resultados
            double msUniform = timeUniform / 1_000_000.0;
            double msPareto = timePareto / 1_000_000.0;
            double improvement = (msUniform - msPareto) / msUniform * 100;

            System.out.printf("%-10d | %-12.2f ms | %-12.2f ms | %-9.1f%%\n",
                    N, msUniform, msPareto, improvement);
        }
    }
}