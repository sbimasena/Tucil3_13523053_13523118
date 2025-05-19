# 🚗 Puzzle Rush Hour Solver dengan Pathfinding  
# Tugas Kecil 3 Strategi Algoritma IF2211

## 📌 Deskripsi  
Program ini adalah **solver** untuk permainan **Rush Hour**, yang menggunakan algoritma **Greedy Best First Search (GBFS), Uniform Cost Search (UCS), A-Star, dan Iterative Deepening A-Star** untuk mencari solusi dari susunan piece pada papan permainan.  
Program membaca input dari file `.txt`, validasi input, mencoba semua kemungkinan gerakan dari piece, serta menampilkan atau menyimpan salah satu solusi dalam bentuk `.txt`.  

## 🛠 Struktur Program
Berikut adalah struktur program tugas kecil ini :
```sh
/Tucil3_13523053_13523118  
├── /bin                          # Compiled .class
│   ├── AnimationListener.class
│   ├── AStar.class    
│   ├── GameAnimation.class       
│   ├── GamePanel.class
│   ├── GBFS.class
│   ├── IDAStar.class
│   ├── RushHourGame.class
│   ├── RushHourGUI.class
│   ├── RushHourIO.class
│   ├── RushHourSolver.class
│   ├── SearchAlgorithm.class
│   └── UCS.class
├── /doc                          # Laporan Tucil
├── /output                       # Hasil output dari program
├── /src                          # Source code program
│   ├── AStar.java     
│   ├── GameAnimation.java        
│   ├── GamePanel.java
│   ├── GBFS.java
│   ├── IDAStar.java
│   ├── RushHourGame.java
│   ├── RushHourGUI.java
│   ├── RushHourIO.java
│   ├── RushHourSolver.java
│   ├── SearchAlgorithm.java
│   └── UCS.java
├── /test                         # Test case
└── README.md                     # Dokumentasi projek
```

## Getting Started 🌐
Berikut instruksi instalasi dan penggunaan program

### Prerequisites

Pastikan anda sudah memiliki:
- **Java 8 atau lebih baru**
- **IDE atau terminal** untuk menjalankan program

### Installation
1. **Clone repository ke dalam suatu folder**

```bash
  https://github.com/sbimasena/Tucil3_13523053_13523118.git
```

2. **Pergi ke directory /Tucil3_13523053_13523118**

```bash
  cd Tucil3_13523053_13523118
```

3. **Compile program**

```bash
  javac src/*.java -d bin 
```

4. **Jalankan program**

GUI Game
```bash
  java -cp bin RushHourGUI
```
CLI Game
```bash
  java -cp bin RushHourSolver
```

## **📌 Cara Penggunaan**

1. **Jalankan program** melalui terminal atau IDE yang mendukung Java.
2. File input **disarankan** berada di dalam folder input dan output akan disimpan dalam folder output
3. **Piih nama file input** pada tombol 'Load Puzzle' misalnya: input.txt
4. Program akan membaca dan memvalidasi format input serta mencoba menyelesaikan puzzle.
5. Pilih algoritma yang ingin digunakan serta heuristik (jika perlu) lalu klik 'Solve Puzzle'.
6. Jika solusi ditemukan, klik tombol play untuk memulai visualisasi jawaban.
7. Tekan tombol reset jika ingin reset ke initial state.
8. Program akan menanyakan apakah solusi ingin disimpan sebagai:
    Teks (.txt)
9. Masukkan nama file untuk menyimpan hasil, misalnya:
    solution.txt untuk menyimpan sebagai teks

## **✍️ Author**
| Name                              | NIM        |
|-----------------------------------|------------|
| Sakti Bimasena                    | 13523053   |
| Farrel Athalla Putra              | 13523118   |
