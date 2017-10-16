require 'base64'
require 'fileutils'

class SonarqubefortifypluginController < ApplicationController

  def get
    project = Project.by_key(params[:resource])
    # we will have to make sure we use the correct file naming conventions  
    send_file Rails.root.join('fortify-pdf-files', project.key.gsub(':', '-') .gsub('.','-')+ '.pdf'), :type => 'application/pdf', :disposition => 'attachment'
  end

  def store
    uploaded = params[:upload]
    filename = params[:pdfname] || uploaded.original_filename
    # Rails.root is WEB-INF dir
    FileUtils::mkdir_p Rails.root.join('fortify-pdf-files') unless File.exists?(Rails.root.join('fortify-pdf-files'))
    File.open(Rails.root.join('fortify-pdf-files', filename), 'wb') do |file|
      file.write(uploaded.read)
    end
    render :nothing => true, :status => 200
  end
end
