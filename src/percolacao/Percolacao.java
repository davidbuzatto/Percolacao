/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and abrir the template in the editor.
 */
package percolacao;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author David
 */
public class Percolacao {

    private UF uf;
    private int n;
    
    private int sitioVirtualTopo;
    private int sitioVirtualAbaixo;
    
    private boolean[][] sitiosAbertos;
    private int numeroDeSitiosAbertos;
        
    // cria uma grade n por n, com todos os sítios bloqueados
    public Percolacao( int n ) {
        
        if ( n <= 0 ) {
            throw new IllegalArgumentException();
        }
        
        this.n = n;
        sitiosAbertos = new boolean[n][n];
        
        /*
         * além de n*n sítios, mais dois são criados para os sítios virtuais.
         */
        //uf = new QuickFindUF( n * n + 2 );
        //uf = new QuickUnionUF( n * n + 2 );
        uf = new WeightedQuickUnionUF( n * n + 2 );
        //uf = new WeightedQuickUnionPathCompressionUF( n * n + 2 );
        
        // os sítios virtuais são os dois últimos itens
        sitioVirtualTopo = n * n;
        sitioVirtualAbaixo = n * n + 1;
        
        // conectando os sítios virtuais com a primeira e a ultima 
        // linha de sítios
        for ( int i = 0; i < n; i++ ) {
            uf.union( sitioVirtualTopo, i );
            uf.union( sitioVirtualAbaixo, n * n - i - 1 );
        }
        
    }

    // abre um sítio (linha, coluna) se o mesmo ainda não estiver aberto
    public void abrir( int linha, int coluna ) {
        
        if ( !estaAberto( linha, coluna ) ) {
            
            numeroDeSitiosAbertos++;
            
            int sitio = converterParaLinear( linha, coluna );
            sitiosAbertos[linha-1][coluna-1] = true;
            
            // vizinhos
            if ( linha - 1 > 0 ) {
                int acima = ( linha - 2 ) * n + coluna - 1;
                if ( sitiosAbertos[linha-2][coluna-1] ) {
                    uf.union( sitio, acima );
                }
            }
            
            if ( linha + 1 <= n ) {
                int abaixo = linha * n + coluna - 1;
                if ( sitiosAbertos[linha][coluna-1] ) {
                    uf.union( sitio, abaixo );
                }
            }
            
            if ( coluna - 1 > 0 ) {
                int esquerda = ( linha - 1 ) * n + coluna - 2;
                if ( sitiosAbertos[linha-1][coluna-2] ) {
                    uf.union( sitio, esquerda );
                }
            }
            
            if ( coluna + 1 <= n ) {
                int direita = ( linha - 1 ) * n + coluna;
                if ( sitiosAbertos[linha-1][coluna] ) {
                    uf.union( sitio, direita );
                }
            }
            
        }
        
    }

    // o sítio (linha, coluna) está aberto?
    public boolean estaAberto( int linha, int coluna ) {
        
        if ( linha < 1 || linha > n || coluna < 1 || coluna > n ) {
            throw new IllegalArgumentException();
        }
        
        return sitiosAbertos[linha-1][coluna-1];
        
    }

    // o sítio (linha, coluna) está cheio?
    public boolean estaCheio( int linha, int coluna ) {
        
        if ( estaAberto( linha, coluna ) ) {
            return uf.find( converterParaLinear( linha, coluna ) ) == uf.find( sitioVirtualTopo );
        }
        
        return false;
        
    }

    public int numeroDeSitiosAbertos() {
        return numeroDeSitiosAbertos;
    }

    // o sistema percolou?
    public boolean percolou() {
        return uf.find( sitioVirtualTopo ) == uf.find( sitioVirtualAbaixo );
    }
    
    private int converterParaLinear( int linha, int coluna ) {
        return ( linha - 1 ) * n + coluna - 1;
    }
    
    private static void testeGUI( 
            int n, 
            int tamanhoSitio, 
            int tamanhoContorno,
            int tempoPausa, 
            int tamanhoJanela ) {
        
        int laragura = n * tamanhoSitio;
        int altura = n * tamanhoSitio;
        
        double escala = (double) tamanhoJanela / altura;
        
        Percolacao p = new Percolacao( n );
        
        Color corGrade = Color.BLACK;
        Color corSitioAberto = Color.WHITE;
        Color corSitioCheio = new Color( 0, 106, 252 );
        Color corSitioFechado = new Color( 50, 50, 50 );
        Color corFonteStatus = new Color( 65, 3, 87, 200 );
        Color corFundoStatus = new Color( 200, 200, 200, 200 );
        
        Font fonteStatus = new Font( "monospaced", Font.BOLD, 30 );
        Stroke contornoGrade = new BasicStroke( tamanhoContorno );
        
        JFrame janela = new JFrame( "Percolação" );
        FontMetrics fm = janela.getFontMetrics( fonteStatus );
        
        JPanel painelPercolacao = new JPanel(){
                    
            @Override
            protected void paintComponent( Graphics g ) {

                super.paintComponent( g );
                
                Graphics2D g2d = ( Graphics2D ) g.create();
                g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                
                Graphics2D g2dStatus = ( Graphics2D ) g2d.create();
                
                g2d.scale( escala, escala );
                
                for ( int i = 1; i <= n; i++ ) {
                    for ( int j = 1; j <= n; j++ ) {
                        
                        if ( p.estaAberto( i, j ) ) {
                            g2d.setColor( corSitioAberto );
                            if ( p.estaCheio( i, j ) ) {
                                g2d.setColor( corSitioCheio );
                            }
                        } else {
                            g2d.setColor( corSitioFechado );
                        }
                        
                        g2d.fillRect( 
                                ( j - 1 ) * tamanhoSitio, 
                                ( i - 1 ) * tamanhoSitio, 
                                tamanhoSitio, tamanhoSitio );
                        
                    }
                }
                
                g2d.setColor( corGrade );
                g2d.setStroke( contornoGrade );
                
                for ( int i = 0; i <= n; i++ ) {
                    g2d.drawLine( i * tamanhoSitio, 0, 
                            i * tamanhoSitio, altura );
                    g2d.drawLine( 0, i * tamanhoSitio, 
                            laragura, i * tamanhoSitio );
                }
                
                String stringStatus = String.format( "%d/%d: %.2f%%" , 
                        p.numeroDeSitiosAbertos, n * n,
                        (double) p.numeroDeSitiosAbertos / ( n * n ) * 100 );
                
                int larguraStringStatus = fm.stringWidth( stringStatus );
                
                g2dStatus.setColor( corFundoStatus );
                g2dStatus.fillRoundRect( 
                        getWidth() - larguraStringStatus - 40, 
                        getHeight() - 20 - fm.getHeight(), larguraStringStatus + 60, 
                        larguraStringStatus + 40, 
                        10, 10 );
                
                
                g2dStatus.setColor( corFonteStatus );
                g2dStatus.setFont( fonteStatus );
                
                g2dStatus.drawString( stringStatus, 
                        getWidth() - larguraStringStatus - 20, 
                        getHeight() - 20 );
                
                g2d.dispose();
                g2dStatus.dispose();

            }

        };
        
        painelPercolacao.setPreferredSize( new Dimension( tamanhoJanela, tamanhoJanela ) );
        
        janela.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        janela.add( painelPercolacao, BorderLayout.CENTER );
        janela.pack();
        janela.setLocationRelativeTo( null );
        janela.setVisible( true );
        
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                
                Random random = new Random();
                
                new Thread( new Runnable() {
                    
                    @Override
                    public void run() {

                        while ( !p.percolou() ) {

                            //int linha = StdRandom.uniform( 1, n + 1 );;
                            //int coluna = StdRandom.uniform( 1, n + 1 );
                            int linha = 1 + random.nextInt( n );
                            int coluna = 1 + random.nextInt( n );

                            if ( !p.estaAberto( linha, coluna ) ) {
                                p.abrir( linha, coluna );
                            }

                            janela.repaint();

                            if ( tempoPausa > 0 ) {
                                try {
                                    Thread.sleep( tempoPausa );
                                } catch ( InterruptedException exc ) {

                                }
                            }

                        }
                        
                        janela.repaint();

                    }

                } ).start();
                
            }
            
        });
        
    }
    
    public static void main( String[] args ) {
        testeGUI( 150, 6, 1, 2, 600 );
    }
    
}
