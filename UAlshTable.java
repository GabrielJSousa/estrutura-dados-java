package aed.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.Random;

class UAlshBucket<Key,Value> implements IUAlshBucket<Key,Value>{

    Key key;
    Value value;
    int hcode1;
    int hcode2;
    boolean isDeleted;
    //int maxST; // highestBrotherWhoHoldsAKey
    int accessCount;

    public UAlshBucket() {
        this.key = null;
        //this.maxST = 0;
        this.accessCount = 0;
        this.isDeleted = false;
    }

    public UAlshBucket (Key key, Value value, int h1, int h2){
        this.key = key;
        this.value = value;
        this.hcode1 = h1;
        this.hcode2 = h2;
        this.isDeleted = false;
        //this.maxST = 0;
        this.accessCount = 0;
    }

    @Override public Key getKey() {return key;}
    @Override public Value getValue() {return value;}
    @Override public boolean isEmpty() {return key == null;}
    @Override public boolean isDeleted() {return isDeleted;}

    public void markDeleted() {
        this.isDeleted = true;
        this.value = null;
    }

    public void reactivate(Value value) {
        this.value = value;
        this.isDeleted = false;
    }

    public void setValue(Value value){
        this.value = value;
    }

}

public class UAlshTable<Key,Value> {

    private int size;
    private int deletedCount;
    private Function<Key, Integer> hc2;

    // ---  VARIÁVEIS PARA O RELATÓRIO (Parte C) ---
    private long nComparisons = 0; // Conta chamadas a .equals()
    private long nSearches = 0;    // Conta chamadas a métodos de pesquisa
    private long nPuts = 0;        // Conta chamadas a put

    // Métodos para aceder e limpar estatísticas
    public void resetStats() {
        this.nComparisons = 0;
        this.nSearches = 0;
        this.nPuts = 0;
    }
    public long getComparisons() { return nComparisons; }
    public long getSearches() { return nSearches; }
    public long getPuts() { return nPuts; }
    // ------------------------------------------------------

    private UAlshBucket<Key, Value>[] t1, t2, t3, t4, t5;
    private int primeIndexT1 = 4; // Começa em 37

    private static final int[] primes = {
            5, 7, 11, 17, 37, 79, 163, 331,
            673, 1361, 2729, 5471, 10949,
            21911, 43853, 87719, 175447, 350899,
            701819, 1403641, 2807303, 5614657,
            11229331, 22458671, 44917381, 89834777, 179669557
    };

    @SuppressWarnings("unchecked")
    public UAlshTable(Function<Key, Integer> hc2) {
        this.hc2 = hc2;
        this.size = 0;
        this.deletedCount = 0;

        // Start at index 4 (37)
        t1 = (UAlshBucket<Key,Value>[]) new UAlshBucket[37];
        t2 = (UAlshBucket<Key,Value>[]) new UAlshBucket[17];
        t3 = (UAlshBucket<Key,Value>[]) new UAlshBucket[11];
        t4 = (UAlshBucket<Key,Value>[]) new UAlshBucket[7];
        t5 = (UAlshBucket<Key,Value>[]) new UAlshBucket[5];

        initTables();
    }

    @SuppressWarnings("unchecked")
    private void initTables() {
        t1 = new UAlshBucket[getCap(0)]; for(int i=0;i<t1.length;i++) t1[i]=new UAlshBucket<>();
        t2 = new UAlshBucket[getCap(1)]; for(int i=0;i<t2.length;i++) t2[i]=new UAlshBucket<>();
        t3 = new UAlshBucket[getCap(2)]; for(int i=0;i<t3.length;i++) t3[i]=new UAlshBucket<>();
        t4 = new UAlshBucket[getCap(3)]; for(int i=0;i<t4.length;i++) t4[i]=new UAlshBucket<>();
        t5 = new UAlshBucket[getCap(4)]; for(int i=0;i<t5.length;i++) t5[i]=new UAlshBucket<>();
    }

    private int getCap(int i) {
        int idx = primeIndexT1 - i;
        return (idx >= 0) ? primes[idx] : primes[0];
    }


    @SuppressWarnings("unchecked")
    private UAlshBucket<Key, Value>[] getTable(int i) {
        switch (i) {
            case 1: return t1;
            case 2: return t2;
            case 3: return t3;
            case 4: return t4;
            case 5: return t5;
            default: throw new IllegalArgumentException("Invalid table index");
        }
    }

    public IUAlshBucket<Key, Value>[] getSubTable(int i) {return getTable(i);}

    // Retorna a posição da chave em cada tabela
    private int getPos(int h1, int h2, int i) {
        UAlshBucket<Key,Value>[] m = getTable(i);
        return ((h1 + i * h2) & 0x7fffffff) % m.length;
    }

    // Ajuda para encontrar até que tabela pesquisar
    private int calculateZ(int h1, int h2) {
        int z = 5;
        for (int i = 1; i <= 5; i++) {
            int pos = getPos(h1, h2, i);
            UAlshBucket<Key, Value> b = getTable(i)[pos];

            if (b.accessCount < z){
                z = b.accessCount;
            }

        }
        return z;
    }

    public boolean containsKey(Key k) {return get(k) != null;}

    public Value get(Key k) {
        if (k == null) return null;

        nSearches++;

        int h1 = k.hashCode();
        int h2 = hc2.apply(k);

        int z = calculateZ(h1, h2);
        if (z == 0) return null;

        // Procura de Z até 1 (Otimização)
        for(int i = z; i >= 1; i--) {
            int pos = getPos(h1, h2, i);
            UAlshBucket<Key, Value> b = getTable(i)[pos];

            b.accessCount++;

            // Verifica: Não Vazio E Não Apagado
            if (!b.isEmpty() && !b.isDeleted()) {
                nComparisons++;
                if (b.hcode1 == h1 && b.hcode2 == h2 && b.getKey().equals(k)) {
                    return b.value;
                }
            }

        }
        return null;
    }


    public void put(Key k, Value v) {
        if (k == null) return;
        if (v == null) {
            delete(k);
            return;
        }

        nPuts++;

        if (size >= t1.length * 0.85) resize(true);

        int h1 = k.hashCode();
        int h2 = hc2.apply(k);

        // Verificar se a chave já existe (ativa ou apagada)
        // Usamos calculateZ para saber até onde procurar
        int z = calculateZ(h1, h2);
        if (z > 0) {
            for (int i = 1; i <= z; i++) {
                int pos = getPos(h1, h2, i);
                UAlshBucket<Key, Value> b = getTable(i)[pos];

                b.accessCount++;

                // Verifica identidade da chave (mesmo que isDeleted seja true)
                if (!b.isEmpty() && b.hcode1 == h1 && b.hcode2 == h2) {
                    nComparisons++;

                    /*
                    O código retornava sempre mesmo que não tenha a chave correta, então ele reescrevia o valor da chave incorreta e não inseria a chave
                    if (!b.getKey().equals(k)) {
                        b.setValue(v);
                        return;
                    }
                     */

                    if (b.getKey().equals(k)) {
                        b.setValue(v);
                        return;
                    }

                    // Se estava apagada, temos de a "ressuscitar"
                    if (b.isDeleted()) {
                        b.isDeleted = false; // Tornar ativa
                        deletedCount--;      // Menos um lixo
                        size++;              // Mais um elemento útil

                    }
                    return;
                }
            }
        }

        // 2. INSERÇÃO NOVA: Procurar slot vazio ou apagado
        int targetTable = -1;
        int targetPos = -1;
        for (int i = 1; i <= 5; i++) {
            int pos = getPos(h1, h2, i);
            UAlshBucket<Key, Value> b = getTable(i)[pos];

            b.accessCount++;

            if (b.isEmpty() || b.isDeleted()) {
                targetTable = i;
                targetPos = pos;
                break;
            }
        }

        // Se não houver espaço, resize e tentar de novo
        if (targetTable == -1) {
            resize(true);
            nPuts--;
            put(k, v);
            return;
        }

        // Inserir no bucket encontrado
        UAlshBucket<Key, Value> bucket = getTable(targetTable)[targetPos];
        //int oldMax = bucket.maxST;

        // Verificar SE estava apagado ANTES de mudar a flag
        if (bucket.isDeleted()) {
            deletedCount--;
        }


        bucket.key = k;
        bucket.value = v;
        bucket.hcode1 = h1;
        bucket.hcode2 = h2;
        bucket.isDeleted = false; // Agora marcamos como ativo

        //bucket.maxST = Math.max(oldMax, targetTable);
        size++;

        /*// 3. ATUALIZAR IRMANDADE
        // Garantir que todos os buckets associados a esta chave sabem que
        // há dados até à tabela 'targetTable'
        for (int i = 1; i <= 5; i++) {
            int pos = getPos(h1, h2, i);
            UAlshBucket<Key, Value> b = getTable(i)[pos];
            if (b.maxST < targetTable) {
                b.maxST = targetTable;
            }
        }*/
    }

    public void fastPut(Key k, Value v) {
        if (k == null || v == null) return;

        int h1 = k.hashCode();
        int h2 = hc2.apply(k);
        int targetTable = -1;
        int targetPos = -1;
        for(int i = 1; i <= 5; i++){
            int pos=getPos(h1, h2, i);
            if(getTable(i)[pos].isEmpty()){
                targetTable = i;
                targetPos = pos;
                break;
            }
        }
        if(targetTable==-1) {
            resize(true);
            fastPut(k,v);
            return;
        }

        UAlshBucket<Key,Value> b=getTable(targetTable)[targetPos];
        //int oldMax=b.maxST;
        b.key=k; b.value=v; b.hcode1=h1; b.hcode2=h2; b.isDeleted=false;
        //b.maxST=Math.max(oldMax,targetTable);
        size++;

        /*for(int i = 1; i <= 5; i++){
            int pos=getPos(h1, h2, i);
            UAlshBucket<Key,Value> sibling = getTable(i)[pos];
            if(sibling.maxST<targetTable) sibling.maxST=targetTable;
        }*/

    }

    public void delete(Key k) {
        if(k==null) return;
        int h1=k.hashCode();
        int h2=hc2.apply(k);
        int z=calculateZ(h1,h2);
        if(z==0) return;

        boolean removed=false;
        for(int i=1;i<=z;i++){
            int pos=getPos(h1,h2,i);
            UAlshBucket<Key,Value> b=getTable(i)[pos];

            b.accessCount++;

            /*
            Corrigiu-se, pois realizava o mesmo if, na condição do primeiro era b.getKey().equals(k) e no segundo if era a mesma condição
            if(!b.isEmpty() && !b.isDeleted() && b.hcode1==h1 && b.hcode2==h2 && b.getKey().equals(k)){
                nComparisons++;

                if (b.getKey().equals(k)) {
                    b.markDeleted();
                    size--;
                    deletedCount++;
                    removed = true;
                    break;
                }

            }
            */

            if(!b.isEmpty() && !b.isDeleted() && b.hcode1==h1 && b.hcode2==h2){
                nComparisons++;

                if (b.getKey().equals(k)) {
                    b.markDeleted();
                    size--;
                    deletedCount++;
                    removed = true;
                    break;
                }

            }
        }

        if(removed && size < t1.length / 4 && primeIndexT1 > 4) resize(false);
    }

    @SuppressWarnings("unchecked")
    private void resize(boolean grow) {
        UAlshBucket<Key,Value>[] oldT1=t1, oldT2=t2, oldT3=t3, oldT4=t4, oldT5=t5;
        if(grow){ if(primeIndexT1<primes.length-1) primeIndexT1++; }
        else{ if(primeIndexT1>4) primeIndexT1--; }

        size=0; deletedCount=0;
        initTables();
        reinserir(oldT1); reinserir(oldT2); reinserir(oldT3); reinserir(oldT4); reinserir(oldT5);
    }

    private void reinserir(UAlshBucket<Key,Value>[] old) {

        for(UAlshBucket<Key,Value> b:old){
            if(b!=null && !b.isEmpty() && !b.isDeleted()) fastPut(b.key,b.value);
        }

    }

    public Iterable<Key> keys() {
        List<Key> list=new ArrayList<>();
        for(int i = 1; i <= 5; i++){
            for(UAlshBucket<Key,Value> b : getTable(i)){
                if(!b.isEmpty() && !b.isDeleted()){
                    list.add(b.key);
                }
            }
        }
        return list;
    }

    public int size() {return size;}
    public int getMainCapacity() {return t1.length;}
    public int getTotalCapacity() {return t1.length + t2.length + t3.length + t4.length + t5.length;}
    public float getLoadFactor() {return ((float) size + (float) deletedCount) / getTotalCapacity();}
    public int getDeletedNotRemoved() {return deletedCount;}

    public void display() {
        System.out.println("\nTabela 1 (T1 - tamanho " + t1.length + "):");
        for(int i = 0; i < t1.length; i++){
            if(!t1[i].isEmpty() && !t1[i].isDeleted()) {
                System.out.println(i + ": " + t1[i].key);
            }
        }
        System.out.println("\nTabela 2 (T2 - tamanho " + t2.length + "):");
        for(int i = 0; i < t2.length; i++){
            if(!t2[i].isEmpty() && !t2[i].isDeleted()){
                System.out.println(i + ": " + t2[i].key);
            }
        }
        System.out.println("\nEstatísticas:");
        System.out.println("Tamanho: " + size());
        System.out.println("Capacidade Principal: " + getMainCapacity());
        System.out.println("Capacidade Total: " + getTotalCapacity());
        System.out.println("Fator de Carga: " + getLoadFactor());
        System.out.println("Elementos Apagados: " + getDeletedNotRemoved());
    }

    public static void main(String[] args) {
        UAlshTable<String,Integer> tabela = new UAlshTable<>(key -> {
            int hash=0;
            for(int i = 0; i < key.length(); i++){
                hash += key.charAt(i);
            }
            return hash;
        });

        System.out.println("=== TESTE DA TABELA HASH ===");

        tabela.put("Teste",10);
        tabela.put("Java",20);
        tabela.put("Hash",30);
        tabela.put("Table",40);

        System.out.println("\nApós inserções:");
        tabela.display();

        System.out.println("\n=== TESTES DE BUSCA ===");
        System.out.println("Contém 'Teste': " + tabela.containsKey("Teste"));
        System.out.println("Valor 'Java': " + tabela.get("Java"));
        System.out.println("Contém 'Inexistente': " + tabela.containsKey("Inexistente"));

        System.out.println("\n=== DELETE ===");
        tabela.delete("Hash");
        System.out.println("Após apagar 'Hash':");
        tabela.display();

        System.out.println("\n=== FASTPUT ===");
        tabela.fastPut("Novo",99);
        tabela.display();

        System.out.println("\n=== UPDATE COM PUT ===");
        tabela.put("Java",200);
        System.out.println("Valor atualizado de 'Java': " + tabela.get("Java"));

        System.out.println("\n=== PUT COM NULL (delete) ===");
        tabela.put("Table",null);
        tabela.display();

        System.out.println("\n=== TODAS AS CHAVES ===");
        for(String k:tabela.keys()){
            System.out.println("Chave: " + k + ", Valor: " + tabela.get(k));
        }

        System.out.println("\n=== TESTE REDIMENSIONAMENTO ===");
        for(int i = 0; i < 50; i++) {
            tabela.put("Key" + i,i);
        }
        System.out.println("Após inserir 50 chaves:");
        tabela.display();
        for(int i = 0; i < 45; i++){
            tabela.delete("Key" + i);
        }
        System.out.println("Após remover 45 chaves:");
        tabela.display();


        System.out.println(">>> INÍCIO DOS TESTES DA PARTE C (PROBLEMA B - Hash Table) <<<");

        // 1. Configurar Tabela
        UAlshTable<String,Integer> table = new UAlshTable<>(key -> {
            int hash=0;
            for(int i = 0; i < key.length(); i++) hash += key.charAt(i);
            return hash;
        });

        int N = 100_000;
        String[] keys = new String[N];
        Random rnd = new Random(12345); // Seed fixa para resultados consistentes

        System.out.println("A gerar " + N + " chaves aleatórias...");
        for(int i=0; i<N; i++) {
            keys[i] = "Key-" + rnd.nextInt(10000000) + "-" + i;
        }

        // 2. Teste de Inserção
        table.resetStats();
        for(int i=0; i<N; i++) {
            table.put(keys[i], i);
        }

        System.out.println("\n--- 1. Análise de Inserção ---");
        System.out.println("Total Inserções: " + table.getPuts());
        System.out.println("Total Comparações (.equals): " + table.getComparisons());
        double avgIns = (double)table.getComparisons() / table.getPuts();
        System.out.printf("Média de comparações por Inserção: %.5f\n", avgIns);

        // 3. Teste de Pesquisa (Sucesso)
        table.resetStats();
        for(int i=0; i<N; i++) {
            table.get(keys[i]);
        }

        System.out.println("\n--- 2. Análise de Pesquisa (Sucesso) ---");
        System.out.println("Total Pesquisas: " + table.getSearches());
        System.out.println("Total Comparações (.equals): " + table.getComparisons());
        double avgSuc = (double)table.getComparisons() / table.getSearches();
        System.out.printf("Média de comparações por Pesquisa: %.5f\n", avgSuc);

        // 4. Teste de Pesquisa (Insucesso)
        table.resetStats();
        for(int i=0; i<N; i++) {
            table.get("NaoExiste-" + i);
        }

        System.out.println("\n--- 3. Análise de Pesquisa (Insucesso) ---");
        System.out.println("Total Pesquisas: " + table.getSearches());
        System.out.println("Total Comparações (.equals): " + table.getComparisons());
        double avgFail = (double)table.getComparisons() / table.getSearches();
        System.out.printf("Média de comparações por Pesquisa: %.5f\n", avgFail);

        System.out.println("\n>>> FIM DOS TESTES <<<");
    }
}