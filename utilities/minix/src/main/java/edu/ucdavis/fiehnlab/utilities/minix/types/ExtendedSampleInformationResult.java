package edu.ucdavis.fiehnlab.utilities.minix.types;

/**
 * Created by wohlgemuth on 2/8/17.
 */
public class ExtendedSampleInformationResult extends SampleInformationResult {

    private String owner;

    private String institute;

    private String sampleDate;

    private String phone;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getSampleDate() {
        return sampleDate;
    }

    public void setSampleDate(String sampleDate) {
        this.sampleDate = sampleDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("SampleInformationResult{" + "sample='").append(sample).append('\'').append(", experimentId=").append(experimentId).append(", description='").append(description).append('\'').append(", species='").append(species).append('\'').append(", organ='").append(organ).append('\'').append(", label='").append(label).append('\'').append(", comment='").append(comment).append('\'').append(", class='").append(className).append('\'').append(", treatment='").append(treatmentName).append('\'').append(", owner ='").append(owner).append('\'').append(", institute value='").append(institute).append('\'').append(", email value='").append(email).append('\'').append(", phone value='").append(phone).append('\'').append(", annotations= {");

        if (annotations != null) {

            if (verbose) {
                for (BinBaseResult x : annotations) {
                    buffer.append("\t" + x.toString() + "\n");
                }
                buffer.append("\t" + "treatment=" + "treatmentName" + "\n");
            } else {
                buffer.append(annotations.size());
            }
        }
        buffer.append('}');

        return buffer.toString();
    }
}
