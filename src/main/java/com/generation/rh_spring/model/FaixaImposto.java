package com.generation.rh_spring.model;

public class FaixaImposto {
    
	private double limite;
    private double aliquota;
    private double deducao;

    public FaixaImposto(double limite, double aliquota, double deducao) {
        this.limite = limite;
        this.aliquota = aliquota;
        this.deducao = deducao;
    }

	public double getLimite() {
		return limite;
	}

	public void setLimite(double limite) {
		this.limite = limite;
	}

	public double getAliquota() {
		return aliquota;
	}

	public void setAliquota(double aliquota) {
		this.aliquota = aliquota;
	}

	public double getDeducao() {
		return deducao;
	}

	public void setDeducao(double deducao) {
		this.deducao = deducao;
	}

}
